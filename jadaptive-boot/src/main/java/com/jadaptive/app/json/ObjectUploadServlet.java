package com.jadaptive.app.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.files.FileAttachment;
import com.jadaptive.api.files.FileAttachmentService;
import com.jadaptive.api.json.RedirectStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.json.UUIDStatus;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.app.db.DocumentHelper;
import com.jadaptive.app.db.MongoEntity;
import com.jadaptive.utils.FileUtils;
import com.jadaptive.utils.ParameterHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="objectServlet", description="Servlet for handing objects", 
	urlPatterns = { "/app/api/form/multipart/*", "/app/api/form/stash/*",
			"/app/api/form/stash-child/*"})
public class ObjectUploadServlet extends HttpServlet {

	private static final long serialVersionUID = -8476101184614381108L;

	private static Logger log = LoggerFactory.getLogger(ObjectUploadServlet.class);
	
	private static final String MULTIPART = "multipart";
	private static final String STASH = "stash";
	private static final String STASH_CHILD = "stash-child";
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ObjectService objectService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private FileAttachmentService fileService;
	
	ObjectMapper json = new ObjectMapper();
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		
		String uri = request.getRequestURI();
		List<String> paths = new ArrayList<>(getPathElements(uri));
		if(paths.size() < 5) {
			throw new IllegalStateException("Too few arguments for stash-child handler!");
		}
		
		String handler = paths.get(3);
		String resourceKey = paths.get(4);
		
		Map<String,String[]> parameters = new HashMap<>();
		
		DocumentHelper.enableMultipleValidation();
		
		try(var scope = SessionUtils.scopedIoWithoutSessionTimeout(request)) {

			generateFormParameters(request, parameters, resourceKey);
			
			sessionUtils.verifySameSiteRequest(request, parameters, resourceKey);
			
			resp.setStatus(200);
			resp.setContentType("application/json");
			String uuid = "";
			
			try {
				switch(handler) {
				case MULTIPART:
					uuid = processMultipartObject(request, resourceKey, parameters);
					break;
				case STASH:
					uuid = processStashedObject(request, resourceKey, parameters);
					break;
				case STASH_CHILD:
					uuid = processStashedChildObject(request, resourceKey, paths, parameters);
					break;
				default:
					uuid = processUrlEncodedForm(request, handler, resourceKey, parameters);
				}

				json.writer().writeValue(resp.getOutputStream(), new UUIDStatus(uuid));
				
				
		    } catch(ValidationException ex) { 
				Feedback.error(ex.getMessage());
				json.writer().writeValue(resp.getOutputStream(), new RequestStatusImpl(false, ex.getMessage()));
			} catch (UriRedirect e) {
				json.writer().writeValue(resp.getOutputStream(), new RedirectStatus(e.getUri()));
			} catch (ObjectException e) {
				Feedback.error(e.getMessage());
				json.writer().writeValue(resp.getOutputStream(), new RequestStatusImpl(false, e.getMessage()));
			} catch (Throwable e) {
				Feedback.error(e.getMessage());
				if(log.isErrorEnabled()) {
					log.error("POST api/objects/{}", resourceKey, e);
				}
				json.writer().writeValue(resp.getOutputStream(), new RequestStatusImpl(false, e.getMessage()));
			} finally {
				DocumentHelper.disableMultipleValidation();
			}
		}
	}

	private List<String> getPathElements(String uri) {
		return new ArrayList<String>(Arrays.asList(FileUtils.checkStartsWithNoSlash(uri).split("/")));
	}

	private String processUrlEncodedForm(HttpServletRequest request, String handler, String resourceKey, Map<String,String[]> parameters) throws ObjectException, ValidationException, IOException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		
		return objectService.getFormHandler(handler).saveObject(DocumentHelper.convertDocumentToObject(
				templateService.getTemplateClass(resourceKey), 
				new Document(DocumentHelper.buildRootObject(parameters, template.getResourceKey(), template).getDocument())));
	}

	private String processMultipartObject(HttpServletRequest request, String resourceKey, Map<String,String[]> parameters) throws ValidationException, IOException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		AbstractObject obj = DocumentHelper.buildRootObject(parameters, template.getResourceKey(), template);
		objectService.saveOrUpdate(obj);
		return obj.getUuid();
	}
	
	private String processStashedObject(HttpServletRequest request, String resourceKey,
			Map<String, String[]> parameters) throws ValidationException, RepositoryException, ObjectException, IOException {
		ObjectTemplate template = templateService.get(resourceKey);
		AbstractObject obj = DocumentHelper.buildRootObject(parameters, template.getResourceKey(), template);
		objectService.stashObject(obj);
		return obj.getUuid();
	}

	private String processStashedChildObject(HttpServletRequest request,  String resourceKey, List<String> paths, Map<String, String[]> parameters) 
			throws ValidationException, IOException {
		
		if(paths.size() < 7) {
			throw new IllegalStateException("Too few arguments for stash-child handler!");
		}
		
		String childResource = paths.get(5);
		String fieldName = paths.get(6);
		
		try {
			ObjectTemplate parentTemplate = templateService.get(resourceKey);
			ObjectTemplate childTemplate = templateService.get(childResource);
			AbstractObject childObject = DocumentHelper.buildRootObject(parameters, childTemplate.getResourceKey(), childTemplate);
			FieldTemplate fieldTemplate = parentTemplate.getField(fieldName);
			Object stashedObject = Request.get().getSession().getAttribute(resourceKey);
			if(Objects.isNull(stashedObject)) {
				throw new IllegalStateException("No parent object found for " + resourceKey);
			}
			if(!(stashedObject instanceof AbstractObject)) {
				Document doc = new Document();
				DocumentHelper.convertObjectToDocument((UUIDDocument) stashedObject, doc);
				stashedObject = new MongoEntity(doc);
			}
			AbstractObject parentObject = (AbstractObject) stashedObject;
			
			if(fieldTemplate.getCollection()) {
				AbstractObject existing = null;
				for(AbstractObject child : parentObject.getObjectCollection(fieldName)) {
					if(Objects.nonNull(child.getUuid()) && child.getUuid().equalsIgnoreCase(childObject.getUuid())) {
						existing = child;
					}
				}
				if(Objects.nonNull(existing)) {
					parentObject.removeCollectionObject(fieldName, existing);
				}
				parentObject.addCollectionObject(fieldName, childObject);
			} else {
				/**
				 * Can this happen?
				 */
				parentObject.setValue(fieldTemplate, childObject);
			}
			
			Feedback.info(childTemplate.getBundle(), fieldName + ".stashed");
			
			objectService.stashObject(parentObject);
			return childObject.getUuid();
		}  finally {
			DocumentHelper.disableMultipleValidation();
		}
	}
	
	private Collection<FileAttachment> generateFormParameters(HttpServletRequest req, Map<String,String[]> parameters, String template) throws IOException {
		
		// Create a new file upload handler
		JakartaServletFileUpload<?,?> upload = new JakartaServletFileUpload<>();

		var attachments = new ArrayList<FileAttachment>();

		FileItemInputIterator iter = upload.getItemIterator(req);

		while(iter.hasNext()) {
		    FileItemInput item = iter.next();

		    if (item.isFormField()) {
		    	
		    	String name = item.getFieldName();
		        String value = IOUtils.toString(item.getInputStream(), "UTF-8");
		        
		        if(log.isInfoEnabled()) {
		        	log.info("Form input {} with value {}", name, value);
		    	}

		        ParameterHelper.setValue(parameters, name, value);
		    } else {
		    	if(StringUtils.isNotBlank(item.getName())) {
				    FileAttachment attachment = fileService.createAttachment(item.getInputStream(), item.getName(), item.getContentType(), item.getFieldName(), template);
			    	ParameterHelper.setValue(parameters, item.getFieldName(), attachment.getUuid());
			    	ParameterHelper.setValue(parameters, item.getFieldName() + "_name", attachment.getFilename());
			    	
			    	if(log.isInfoEnabled()) {
			        	log.info("File input {} with value {}", item.getFieldName(), item.getName());
			    	}
			    	attachments.add(attachment);
		    	}
		    }
		    
		}
		
		return attachments;
	}
	

}
