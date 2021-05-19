package com.jadaptive.app.json;

import java.io.IOException;
import java.util.Collection;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.json.BootstrapTableController;
import com.jadaptive.api.json.BootstrapTablePageProcessor;
import com.jadaptive.api.json.BootstrapTableResult;
import com.jadaptive.api.json.RedirectStatus;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.app.db.DocumentHelper;
import com.jadaptive.app.entity.MongoEntity;

@Controller
public class ObjectController extends BootstrapTableController<AbstractObject>{

	static Logger log = LoggerFactory.getLogger(ObjectController.class);
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ObjectService entityService; 
	
	@Autowired
	private DocumentHelper documentHelper; 
	
	@ExceptionHandler(AccessDeniedException.class)
	public void handleException(HttpServletRequest request, 
			HttpServletResponse response,
			AccessDeniedException e) throws IOException {
	   response.sendError(HttpStatus.FORBIDDEN.value(), e.getMessage());
	}

	
	@RequestMapping(value="/app/api/{resourceKey}/{uuid}", method = RequestMethod.GET, produces = {"application/json;charset-UTF-8"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<AbstractObject> getEntity(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
		   return new EntityStatus<AbstractObject>(entityService.get(resourceKey, uuid));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/{}", resourceKey, uuid, e);
			}
			return new EntityStatus<AbstractObject>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<AbstractObject> getEntity(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, ObjectException {
		
		if(resourceKey.equals("logon")) {
			return new EntityStatus<AbstractObject>(false, "Logon API requires POST request");
		}
		
		try {
			   return new EntityStatus<AbstractObject>(entityService.getSingleton(resourceKey));
		} catch(Throwable e) {
			return handleException(e, "GET", resourceKey);
		}
	}
	
	private EntityStatus<AbstractObject> handleException(Throwable e, String method, String resourceKey) {
		if(e instanceof AccessDeniedException) {
			log.error("{} api/{} Access Denied", method, resourceKey);
			throw (AccessDeniedException)e;
		}
		if(log.isErrorEnabled()) {
			log.error("{} api/{}", method, resourceKey, e);
		}
		return new EntityStatus<AbstractObject>(false, e.getMessage());
	}
	
	@RequestMapping(value="/app/api/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"},
			consumes = { "application/json" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveObjectFromJSON(HttpServletRequest request, @PathVariable String resourceKey, @RequestBody MongoEntity entity)  {

		try {
			entityService.saveOrUpdate(entity);
			return new RequestStatusImpl();
		} catch (Throwable e) {
			return handleException(e, "POST", resourceKey);
		}
	}
	
	@RequestMapping(value="/app/api/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"},
			consumes = { "application/x-www-form-urlencoded" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveObjectFromForm(HttpServletRequest request, @PathVariable String resourceKey)  {

		try {
			ObjectTemplate template = templateService.get(resourceKey);
			entityService.saveOrUpdate(documentHelper.buildObject(request, template.getResourceKey(), template));
			return new RequestStatusImpl();
		} catch (UriRedirect e) {
			return new RedirectStatus(e.getUri());
		} catch (Throwable e) {
			return handleException(e, "POST", resourceKey);
		}
	}


	@RequestMapping(value="/app/api/{resourceKey}/{uuid}", method = RequestMethod.DELETE, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus delete(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {

		setupUserContext(request);
		
		try {
			entityService.delete(resourceKey, uuid);
			return new RequestStatusImpl();
		} catch (Throwable e) {
			return handleException(e, "DELETE", resourceKey);
		}
	}
	
	@RequestMapping(value="/app/api/{resourceKey}/{name}/{uuid}", method = RequestMethod.DELETE, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus delete(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String name, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {

		setupUserContext(request);
		
		try {
			entityService.delete(resourceKey, uuid);
			return new RequestStatusImpl();
		} catch (Throwable e) {
			return handleException(e, "DELETE", resourceKey);
		}
	}
	
	@RequestMapping(value="/app/api/{resourceKey}/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityResultsStatus<AbstractObject> listObjects(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
			   return new EntityResultsStatus<AbstractObject>(entityService.list(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/list", resourceKey, e);
			}
			return new EntityResultsStatus<AbstractObject>(false, e.getMessage());
		}
	}

	@RequestMapping(value="/app/api/{resourceKey}/personal", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityListStatus<AbstractObject> listPersonal(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
			   return new EntityListStatus<AbstractObject>(entityService.personal(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/list", resourceKey, e);
			}
			return new EntityListStatus<AbstractObject>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/{resourceKey}/table", method = { RequestMethod.POST, RequestMethod.GET }, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public BootstrapTableResult<AbstractObject> tableObjects(HttpServletRequest request, 
			@PathVariable String resourceKey,
			@RequestParam(required=false, defaultValue = "asc") String order,
			@RequestParam(required=false, defaultValue = "0") int offset,
			@RequestParam(required=false, defaultValue = "100") int limit) throws RepositoryException, UnknownEntityException, ObjectException {
		
		try {
			
			ObjectTemplate template = templateService.get(resourceKey);
			
			return processDataTablesRequest(request, 
					template,
				new BootstrapTablePageProcessor() {

					@Override
					public Collection<?> getPage(String searchColumn, String searchPattern, int start,
							int length, String sortBy)
							throws UnauthorizedException,
							AccessDeniedException {
						return entityService.table(resourceKey, searchColumn, searchPattern, offset, limit);
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return entityService.count(resourceKey, searchColumn, searchPattern);
					}
				});

		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/table", resourceKey, e);
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
