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

import com.jadaptive.api.app.I18N;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.files.FileAttachment;
import com.jadaptive.api.files.FileAttachmentService;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.pages.ext.ValidationHelper;
import com.jadaptive.app.db.DocumentHelper;
import com.jadaptive.app.db.MongoEntity;
import com.jadaptive.utils.FileUtils;
import com.jadaptive.utils.ParameterHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class AbstractObjectUploadServlet extends HttpServlet {

	private static final long serialVersionUID = -8476101184614381108L;

	private static Logger log = LoggerFactory.getLogger(AbstractObjectUploadServlet.class);
	
	protected static final String MULTIPART = "multipart";
	protected static final String VALIDATE = "validate";
	protected static final String STASH = "stash";
	protected static final String STASH_CHILD = "stash-child";
	
	@Autowired
	protected TemplateService templateService; 
	
	@Autowired
	private ObjectService objectService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private FileAttachmentService fileService;

	private String handlerPrefix;
	
	protected AbstractObjectUploadServlet(String handlerPrefix) {
		this.handlerPrefix = handlerPrefix;
	}
	
	@Override
	protected final void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		
		String uri = request.getRequestURI();
		List<String> paths = new ArrayList<>(getPathElements(uri));
		if(paths.size() < 5) {
			throw new IllegalStateException("Too few arguments for stash-child handler!");
		}
		
		String resourceKey = paths.get(4);
		
		Map<String,String[]> parameters = new HashMap<>();
		 
		try(@SuppressWarnings("unused") var scope = SessionUtils.scopedIoWithoutSessionTimeout(request)) {
			generateFormParameters(request, parameters, resourceKey);
			
			String handler = decodeHandler(request, resp, paths, resourceKey, parameters);
			if(!handler.startsWith(handlerPrefix)) {
				throw new IllegalStateException("Invalid handler prefix!");
			}
			handler = handler.substring(handlerPrefix.length());
			
			doHandlePost(request, resp, paths, handler, resourceKey, parameters);
		}
	}

	protected String decodeHandler(HttpServletRequest request, HttpServletResponse resp, List<String> paths,
			String resourceKey, Map<String, String[]> parameters) {
		return paths.get(3);
	}
	
	protected abstract void doHandlePost(HttpServletRequest request, HttpServletResponse resp, List<String> paths,
			String handler, String resourceKey, Map<String, String[]> parameters) throws IOException;

	protected String processHandler(HttpServletRequest request, HttpServletResponse resp, List<String> paths,
			String handler, String resourceKey, Map<String, String[]> parameters, String uuid) throws IOException {
		switch(handler) {
		case VALIDATE:
			processValidation(request, resourceKey, parameters);
			break;
		case MULTIPART:
			ValidationHelper.enableMultipleValidation();
			try {
				uuid = processMultipartObject(request, resp, resourceKey, parameters);
			}
			finally {
				ValidationHelper.disableMultipleValidation();
			}
			break;
		case STASH:
			uuid = processStashedObject(request, resourceKey, parameters);
			break;
		case STASH_CHILD:
			uuid = processStashedChildObject(request, resourceKey, paths, parameters);
			break;
		default:
			uuid = processUrlEncodedForm(request, handler, resourceKey, parameters);
			break;
		}
		return uuid;
	}

	private List<String> getPathElements(String uri) {
		return new ArrayList<String>(Arrays.asList(FileUtils.checkStartsWithNoSlash(uri).split("/")));
	}

	private final String processUrlEncodedForm(HttpServletRequest request, String handler, String resourceKey, Map<String,String[]> parameters) throws ObjectException, ValidationException, IOException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		
		sessionUtils.verifySameSiteRequest(request, parameters, template);
		
		UUIDEntity obj = DocumentHelper.convertDocumentToObject(
				templateService.getTemplateClass(resourceKey), 
				new Document(DocumentHelper.buildRootObject(parameters, 
						template.getResourceKey(), template).getDocument()));
		var res = objectService.getFormHandler(handler).saveObject(obj);
		
		/* Regardless of if this is basic POST or a JSON post, we still want
		 * success feedback on the returning page 
		 */
		if(template.isSingleton()) {
			Feedback.success("default", "object.saved", I18N.getResource(
					sessionUtils.getLocale(request), 
					template.getBundle(),
					template.getResourceKey() + ".name"));
		} else {
			Feedback.success("default", "object.saved", generateName(obj, template));
		}

		return res;
	}
	
	private final Object generateName(UUIDEntity obj, ObjectTemplate template) {
		if(obj instanceof NamedDocument nd) {
			return nd.getName();
		} else {
			try {
				return ReflectionUtils.getField(obj.getClass(), template.getNameField()).get(obj);
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			}
			return I18N.getResource(template.getBundle(), String.format("%s.name", template.getResourceKey()));
		}
	}

	private final void processValidation(HttpServletRequest request, String resourceKey, Map<String, String[]> parameters) throws ValidationException, IOException {
		

		ObjectTemplate template = templateService.get(resourceKey);
		request.getSession().removeAttribute(resourceKey);
		
		DocumentHelper.buildRootObject(parameters, template.getResourceKey(), template);

	
	}

	private final String processMultipartObject(HttpServletRequest request, HttpServletResponse response, String resourceKey, Map<String,String[]> parameters) throws ValidationException, IOException {
			
		ObjectTemplate template = templateService.get(resourceKey);
		sessionUtils.verifySameSiteRequest(request, parameters, template);
		AbstractObject obj = DocumentHelper.buildRootObject(parameters, template.getResourceKey(), template);

		if(!ValidationHelper.hasErrors()) {
			String res = objectService.saveOrUpdate(obj);
	
			/* Regardless of if this is basic POST or a JSON post, we still want
			 * success feedback on the returning page 
			 */
			if(template.isSingleton()) {
				Feedback.success("default", "object.saved", I18N.getResource(
						sessionUtils.getLocale(request), 
						template.getBundle(),
						template.getResourceKey() + ".name"));
			} else {
				Feedback.success("default", "object.saved", generateName(obj, template));
			}
			
			return res;
		}
		
		return null;

	}
	
	private final Object generateName(AbstractObject obj, ObjectTemplate template) {

		try {
			return obj.getValue(template.getField(template.getNameField()));
		} catch (Throwable e) {
		}
		
		return I18N.getResource(template.getBundle(), String.format("%s.name", template.getResourceKey()));
		
	}

	private  String processStashedObject(HttpServletRequest request, String resourceKey,
			Map<String, String[]> parameters) throws ValidationException, RepositoryException, ObjectException, IOException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		
		sessionUtils.verifySameSiteRequest(request, parameters, template);
		
		AbstractObject obj = DocumentHelper.buildRootObject(parameters, template.getResourceKey(), template);
		objectService.stashObject(obj);
		return obj.getUuid();
	}

	protected String processStashedChildObject(HttpServletRequest request,  String resourceKey, List<String> paths, Map<String, String[]> parameters) 
			throws ValidationException, IOException {
		
		if(paths.size() < 7) {
			throw new IllegalStateException("Too few arguments for stash-child handler!");
		}
		
		String childResource = paths.get(5);
		String fieldName = paths.get(6);
		
		ObjectTemplate parentTemplate = templateService.get(resourceKey);
		ObjectTemplate childTemplate = templateService.get(childResource);
		
		sessionUtils.verifySameSiteRequest(request, parameters, childTemplate);
		
		
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
		
		objectService.stashObject(parentObject);
		return childObject.getUuid();
		  
	}
	
	protected final Collection<FileAttachment> generateFormParameters(HttpServletRequest req, Map<String,String[]> parameters, String template) throws IOException {
		
		// Create a new file upload handler
		JakartaServletFileUpload<?,?> upload = new JakartaServletFileUpload<>();

		var attachments = new ArrayList<FileAttachment>();

		FileItemInputIterator iter = upload.getItemIterator(req);

		while(iter.hasNext()) {
		    FileItemInput item = iter.next();

		    if (item.isFormField()) {
		    	
		    	String name = item.getFieldName();
		        String value = IOUtils.toString(item.getInputStream(), "UTF-8");
		        
		        if(log.isDebugEnabled()) {
		        	log.debug("Form input {} with value {}", name, value);
		    	}

		        ParameterHelper.setValue(parameters, name, value);
		    } else {
		    	if(StringUtils.isNotBlank(item.getName())) {
				    FileAttachment attachment = fileService.createAttachment(item.getInputStream(), item.getName(), item.getContentType(), item.getFieldName(), template);
			    	ParameterHelper.setValue(parameters, item.getFieldName(), attachment.getUuid());
			    	ParameterHelper.setValue(parameters, item.getFieldName() + "_name", attachment.getFilename());
			    	
			    	if(log.isDebugEnabled()) {
			        	log.debug("File input {} with value {}", item.getFieldName(), item.getName());
			    	}
			    	attachments.add(attachment);
		    	}
		    }
		    
		}
		
		return attachments;
	}
	

}
