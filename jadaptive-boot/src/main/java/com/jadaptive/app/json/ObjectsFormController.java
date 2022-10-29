package com.jadaptive.app.json;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import javax.lang.model.UnknownEntityException;
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

import com.jadaptive.api.app.I18N;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.json.EntityStatus;
import com.jadaptive.api.json.RedirectStatus;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.json.UUIDStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.ErrorPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.app.db.DocumentHelper;

@Controller
public class ObjectsFormController {

static Logger log = LoggerFactory.getLogger(ObjectsJsonController.class);
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ObjectService objectService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
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
			consumes = { "multipart/form-data" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveFormAsObject(HttpServletRequest request, @PathVariable String resourceKey)  {

		try {
			
			sessionUtils.verifySameSiteRequest(request);
			
			ObjectTemplate template = templateService.get(resourceKey);
			request.getSession().removeAttribute(resourceKey);
			AbstractObject obj = DocumentHelper.buildObject(request, template.getResourceKey(), template);
			String uuid = objectService.saveOrUpdate(obj);
			
			if(template.isSingleton()) {
				Feedback.success("default", "object.saved", I18N.getResource(
						sessionUtils.getLocale(request), 
						template.getBundle(),
						template.getResourceKey() + ".name"));
			} else {
				Feedback.success("default", "object.saved", obj.getValue(template.getNameField()));
			}
			
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
	
	@RequestMapping(value="/app/api/form/multipart/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"},
			consumes = { "multipart/form-data" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveFormAsObjectMultipart(HttpServletRequest request, @PathVariable String resourceKey)  {

		try {
			
			sessionUtils.verifySameSiteRequest(request);
			
			ObjectTemplate template = templateService.get(resourceKey);
			request.getSession().removeAttribute(resourceKey);
			
			AbstractObject obj = DocumentHelper.buildObject(request, template.getResourceKey(), template);
			String uuid = objectService.saveOrUpdate(obj);
			
			if(template.isSingleton()) {
				Feedback.success("default", "object.saved", I18N.getResource(
						sessionUtils.getLocale(request), 
						template.getBundle(),
						template.getResourceKey() + ".name"));
			} else {
				Feedback.success("default", "object.saved", obj.getValue(template.getNameField()));
			}
			
			return new UUIDStatus(uuid);
		}  catch(ValidationException ex) { 
			Feedback.error("default", "object.error", ex.getMessage());
			return new RequestStatusImpl(false, ex.getMessage());
		} catch (UriRedirect e) {
			return new RedirectStatus(e.getUri());
		} catch (Throwable e) {
			Feedback.error("default", "object.error", e.getMessage());
			if(log.isErrorEnabled()) {
				log.error("POST api/objects/{}", resourceKey, e);
			}
			Feedback.error("default", "object.error", e.getMessage());
			return new RequestStatusImpl(false, e.getMessage());
		}
	}

	@RequestMapping(value="/app/api/form/cancel/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	public void cancelForm(HttpServletRequest request, HttpServletResponse response, @PathVariable String resourceKey)  {

		try {
			request.getSession().removeAttribute(resourceKey);
			response.sendRedirect("/app/ui/search/" + resourceKey);
		}  catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/objects/{}", resourceKey, e);
			}
			throw new PageRedirect(new ErrorPage(e));
		}
	}
	
	@RequestMapping(value="/app/api/form/stash/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"},
			consumes = { "multipart/form-data" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus stashObject(HttpServletRequest request, 
			@PathVariable String resourceKey)  {

		try {
			ObjectTemplate template = templateService.get(resourceKey);
			AbstractObject obj = DocumentHelper.buildObject(request, template.getResourceKey(), template);
			request.getSession().setAttribute(resourceKey, obj);

			return new UUIDStatus(obj.getUuid());
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
	
	@RequestMapping(value="/app/api/form/stash/{resourceKey}/{childResource}/{fieldName}", method = RequestMethod.POST, produces = {"application/json"},
			consumes = { "multipart/form-data" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus stashEmbeddedObject(HttpServletRequest request, 
			@PathVariable String resourceKey, @PathVariable String childResource, @PathVariable String fieldName)  {

		try {
			ObjectTemplate parentTemplate = templateService.get(resourceKey);
			ObjectTemplate childTemplate = templateService.get(childResource);
			AbstractObject childObject = DocumentHelper.buildObject(request, childTemplate.getResourceKey(), childTemplate);
			FieldTemplate fieldTemplate = parentTemplate.getField(fieldName);
			AbstractObject parentObject = (AbstractObject) request.getSession().getAttribute(resourceKey);
			if(Objects.isNull(parentObject)) {
				throw new IllegalStateException("No parent object found for " + resourceKey);
			}
			if(fieldTemplate.getCollection()) {
				AbstractObject existing = null;
				if(parentObject.hasValue(fieldName)) {
					for(AbstractObject child : parentObject.getObjectCollection(fieldName)) {
						if(child.getUuid().equalsIgnoreCase(childObject.getUuid())) {
							existing = child;
						}
					}
				}
				if(Objects.nonNull(existing)) {
					parentObject.removeCollectionObject(fieldName, existing);
				} else {
					childObject.setUuid(UUID.randomUUID().toString());
				}
				parentObject.addCollectionObject(fieldName, childObject);
			} else {
				/**
				 * Can this happen?
				 */
				parentObject.setValue(fieldTemplate, childObject);
			}
			
			Feedback.info(childTemplate.getBundle(), fieldName + ".stashed");
			
			request.getSession().setAttribute(resourceKey, parentObject);

			return new UUIDStatus(childObject.getUuid());
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
			consumes = { "multipart/form-data" })
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
	
	@RequestMapping(value="/app/api/objects/{resourceKey}/copy/{uuid}", method = RequestMethod.GET, produces = {"application/json"})
	public void copyObject(HttpServletRequest request, HttpServletResponse response, @PathVariable String resourceKey,
			@PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
			sessionUtils.verifySameSiteRequest(request);
			
		    AbstractObject obj = objectService.get(resourceKey, uuid);
		    obj.setUuid(null);
		    obj.getDocument().remove("_id");
		    obj.getDocument().remove("uuid");
		    request.getSession().setAttribute(resourceKey, obj);
		  
		    response.sendRedirect(String.format("/app/ui/create/%s", resourceKey));
		   
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/objects/{}/copy", resourceKey, e);
			}
			throw new ObjectException(e);
		}
	}
	
	
}
