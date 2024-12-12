package com.jadaptive.app.json;

import java.util.Collection;

import javax.lang.model.UnknownEntityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
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
import com.jadaptive.api.json.EntityResultsStatus;
import com.jadaptive.api.json.EntityStatus;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDReference;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.app.db.MongoEntity;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ObjectsJsonController extends BootstrapTableController<AbstractObject>{

	static Logger log = LoggerFactory.getLogger(ObjectsJsonController.class);
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ObjectService objectService;
	
	@Autowired
	private ObjectTemplateRepository templateRepository;
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@RequestMapping(value="/app/api/objects/{resourceKey}/{uuid}", method = RequestMethod.GET, produces = {"application/json;charset-UTF-8"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<AbstractObject> getEntity(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
		   return new EntityStatus<AbstractObject>(objectService.get(resourceKey, uuid));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/objects/{}/{}", resourceKey, uuid, e);
			}
			return new EntityStatus<AbstractObject>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/objects/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<AbstractObject> getEntity(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, ObjectException {
		
		if(resourceKey.equals("logon")) {
			return new EntityStatus<AbstractObject>(false, "Logon API requires POST request");
		}
		
		try {
			   return new EntityStatus<AbstractObject>(objectService.getSingleton(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/objects/{}", resourceKey, e);
			}
			return handleException(e, "GET", resourceKey);
		}
	}
	
	private EntityStatus<AbstractObject> handleException(Throwable e, String method, String resourceKey) {
		if(e instanceof AccessDeniedException) {
			log.error("{} api/objects/{} Access Denied", method, resourceKey);
			throw (AccessDeniedException)e;
		}
		if(log.isErrorEnabled()) {
			log.error("{} api/objects/{}", method, resourceKey, e);
		}
		return new EntityStatus<AbstractObject>(false, e.getMessage());
	}
	
	@RequestMapping(value="/app/api/objects/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"},
			consumes = { "application/json" })
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveObject(HttpServletRequest request, @PathVariable String resourceKey, @RequestBody MongoEntity entity)  {

		try {
			objectService.saveOrUpdate(entity);
			return new RequestStatusImpl();
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/objects/{}", resourceKey, e);
			}
			return handleException(e, "POST", resourceKey);
		}
	}


	@RequestMapping(value="/app/api/objects/{resourceKey}/{uuid}", method = RequestMethod.DELETE, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus delete(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {

		setupUserContext(request);
		
		try {
			sessionUtils.verifySameSiteRequest(request, resourceKey);
		
			objectService.delete(resourceKey, uuid);
			return new RequestStatusImpl();
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("DELETE api/objects/{}", resourceKey, e);
			}
			Feedback.error(e.getMessage());
			return handleException(e, "DELETE", resourceKey);
		}
	}
	
	@RequestMapping(value="/app/api/objects/{resourceKey}/{name}/{uuid}", method = RequestMethod.DELETE, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus delete(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String name, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {

		setupUserContext(request);
		
		try {
			objectService.delete(resourceKey, uuid);
			return new RequestStatusImpl();
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("DELETE api/objects/{}", resourceKey, e);
			}
			return handleException(e, "DELETE", resourceKey);
		}
	}
	
	@RequestMapping(value="/app/api/objects/{resourceKey}/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityResultsStatus<AbstractObject> listObjects(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
			   return new EntityResultsStatus<AbstractObject>(objectService.list(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/objects/{}/list", resourceKey, e);
			}
			return new EntityResultsStatus<AbstractObject>(false, e.getMessage());
		}
	}

//	@RequestMapping(value="/app/api/objects/{resourceKey}/personal", method = RequestMethod.GET, produces = {"application/json"})
//	@ResponseBody
//	@ResponseStatus(value=HttpStatus.OK)
//	public EntityListStatus<AbstractObject> listPersonal(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, ObjectException {
//		try {
//			   return new EntityListStatus<AbstractObject>(entityService.personal(resourceKey));
//		} catch(Throwable e) {
//			if(log.isErrorEnabled()) {
//				log.error("GET api/objects/{}/personal", resourceKey, e);
//			}
//			return new EntityListStatus<AbstractObject>(false, e.getMessage());
//		}
//	}
	
	@RequestMapping(value="/app/api/objects/{resourceKey}/table", method = { RequestMethod.POST, RequestMethod.GET }, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public BootstrapTableResult<AbstractObject> tableObjects(HttpServletRequest request, 
			@PathVariable String resourceKey,
			@RequestParam(required=false, defaultValue = "") String sort,
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
 						return objectService.table(resourceKey, searchColumn, searchPattern, offset, limit, sort, SortOrder.valueOf(order.toUpperCase()));
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return objectService.count(resourceKey, searchColumn, searchPattern);
					}
				});

		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/objects/{}/table", resourceKey, e);
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	@RequestMapping(value="/app/api/references/{resourceKey}/table", method = { RequestMethod.POST, RequestMethod.GET }, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public BootstrapTableResult<UUIDReference> referenceObjects(HttpServletRequest request, 
			@PathVariable String resourceKey,
			@RequestParam(required=false, defaultValue = "") String sort,
			@RequestParam(required=false, defaultValue = "asc") String order,
			@RequestParam(required=false, defaultValue = "0") int offset,
			@RequestParam(required=false, defaultValue = "100") int limit) throws RepositoryException, UnknownEntityException, ObjectException {
		
		try {
			
			ObjectTemplate template = templateService.get(resourceKey);
			
			return processDataReferencesRequest(request, 
					template,
				new BootstrapTablePageProcessor() {

					@Override
					public Collection<?> getPage(String searchColumn, String searchPattern, int start,
							int length, String sortBy)
							throws UnauthorizedException,
							AccessDeniedException {
						setupSystemContext();
						try {
							return objectService.table(resourceKey, template.getNameField(), searchPattern, offset, limit, sort, SortOrder.valueOf(order.toUpperCase()));
						} finally {
							clearUserContext();
						}
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						setupSystemContext();
						try {
							return objectService.count(resourceKey, template.getNameField(), searchPattern);
						} finally {
							clearUserContext();
						}
					}
				});

		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/objects/{}/table", resourceKey, e);
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	@RequestMapping(value="/app/api/templates/{resourceKey}/table", method = { RequestMethod.POST, RequestMethod.GET }, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public BootstrapTableResult<UUIDReference> templateObjects(HttpServletRequest request, 
			@PathVariable String resourceKey,
			@RequestParam(required=false, defaultValue = "") String sort,
			@RequestParam(required=false, defaultValue = "asc") String order,
			@RequestParam(required=false, defaultValue = "0") int offset,
			@RequestParam(required=false, defaultValue = "100") int limit) throws RepositoryException, UnknownEntityException, ObjectException {
		
		try {
			
			ObjectTemplate template = templateService.get(resourceKey);
			
			return processDataReferencesRequest(request, 
					template,
				new BootstrapTablePageProcessor() {

					@Override
					public Collection<?> getPage(String searchColumn, String searchPattern, int start,
							int length, String sortBy)
							throws UnauthorizedException,
							AccessDeniedException {
						setupSystemContext();
						try {
							return objectService.table(ObjectTemplate.RESOURCE_KEY, "parentKey", resourceKey, offset, limit, sort, SortOrder.valueOf(order.toUpperCase()));
						} finally {
							clearUserContext();
						}
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						setupSystemContext();
						try {
							return objectService.count(ObjectTemplate.RESOURCE_KEY, "parentKey", resourceKey);
						} finally {
							clearUserContext();
						}
					}
				});

		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/objects/{}/table", resourceKey, e);
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
