package com.jadaptive.app.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.I18N;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.files.FileAttachment;
import com.jadaptive.api.files.FileAttachmentService;
import com.jadaptive.api.json.RedirectStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.json.UUIDStatus;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.app.db.DocumentHelper;
import com.jadaptive.utils.FileUtils;
import com.jadaptive.utils.ParameterHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="objectServlet", description="Servlet for handing objects", urlPatterns = { "/app/api/form/multipart/*" })
public class ObjectUploadServlet extends HttpServlet {

	private static final long serialVersionUID = -8476101184614381108L;

	private static Logger log = LoggerFactory.getLogger(ObjectUploadServlet.class);
	
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
		
		String resourceKey = FileUtils.lastPathElement(request.getRequestURI());
		Map<String,String[]> parameters = new HashMap<>();
		
		try(var scope = SessionUtils.scopedIoWithoutSessionTimeout(request)) {

			/**
			 * Return the attachments uploaded. TODO stash these in case of validation
			 * failure so they can be attached in subsequent operation
			 */
			@SuppressWarnings("unused")
			var attachments = generateFormParameters(request, parameters);
			
			sessionUtils.verifySameSiteRequest(request, parameters);
			
			resp.setStatus(200);
			resp.setContentType("application/json");
			
			try {
				
				ObjectTemplate template = templateService.get(resourceKey);
				request.getSession().removeAttribute(resourceKey);
				
				AbstractObject obj = DocumentHelper.buildRootObject(parameters, template.getResourceKey(), template);
				String uuid = objectService.saveOrUpdate(obj);
				
				if(template.isSingleton()) {
					Feedback.success("default", "object.saved", I18N.getResource(
							sessionUtils.getLocale(request), 
							template.getBundle(),
							template.getResourceKey() + ".name"));
				} else {
					Feedback.success("default", "object.saved", obj.getValue(template.getNameField()));
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
			}
		}
	}
	
	private Collection<FileAttachment> generateFormParameters(HttpServletRequest req, Map<String,String[]> parameters) throws IOException {
		
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
				    FileAttachment attachment = fileService.createAttachment(item.getInputStream(), item.getName(), item.getContentType(), item.getFieldName());
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
