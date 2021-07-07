package com.jadaptive.app.json;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.json.RedirectStatus;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.app.db.DocumentHelper;

@Controller
public class ObjectsFormController {

static Logger log = LoggerFactory.getLogger(ObjectsJsonController.class);
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ObjectService objectService; 
	
	@ExceptionHandler(AccessDeniedException.class)
	public void handleException(HttpServletRequest request, 
			HttpServletResponse response,
			AccessDeniedException e) throws IOException {
	   response.sendError(HttpStatus.FORBIDDEN.value(), e.getMessage());
	}
	
	private EntityStatus<AbstractObject> handleException(Throwable e, String method, String resourceKey) {
		if(e instanceof AccessDeniedException) {
			log.error("{} api/objects/{} Access Denied", method, resourceKey);
			throw (AccessDeniedException)e;
		}
		if(log.isErrorEnabled()) {
			log.error("{} api/form/{}", method, resourceKey, e);
		}
		return new EntityStatus<AbstractObject>(false, e.getMessage());
	}
	
	@RequestMapping(value="/app/api/form/default/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"},
			consumes = { "application/x-www-form-urlencoded" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveFormAsObject(HttpServletRequest request, @PathVariable String resourceKey)  {

		try {
			ObjectTemplate template = templateService.get(resourceKey);
			String uuid = objectService.saveOrUpdate(DocumentHelper.buildObject(request, template.getResourceKey(), template));
			return new UUIDStatus(uuid);
		}  catch(ValidationException ex) { 
			return new RequestStatusImpl(false, ex.getMessage());
		} catch (UriRedirect e) {
			return new RedirectStatus(e.getUri());
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/objects/{}", resourceKey, e);
			}
			return handleException(e, "POST", resourceKey);
		}
	}
	
	@RequestMapping(value="/app/api/form/{handler}/{resourceKey}", 
			method = RequestMethod.POST, produces = {"application/json"},
			consumes = { "application/x-www-form-urlencoded" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveObjectFromForm(HttpServletRequest request, 
			@PathVariable String handler, 
			@PathVariable String resourceKey)  {

		try {
			ObjectTemplate template = templateService.get(resourceKey);
			AbstractObject obj = DocumentHelper.buildObject(request, template.getResourceKey(), template);
			objectService.getFormHandler(handler).saveObject(DocumentHelper.convertDocumentToObject(
					templateService.getTemplateClass(resourceKey), 
					new Document(obj.getDocument())));
			return new UUIDStatus(obj.getUuid());
		} catch(ValidationException ex) { 
			return new RequestStatusImpl(false, ex.getMessage());
		} catch (UriRedirect e) {
			return new RedirectStatus(e.getUri());
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/form/{}", resourceKey, e);
			}
			return handleException(e, "POST", resourceKey);
		}
	}
}
