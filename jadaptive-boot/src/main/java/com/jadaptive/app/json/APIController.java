package com.jadaptive.app.json;

import java.io.IOException;
import java.util.Collection;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityService;
import com.jadaptive.api.json.BootstrapTableController;
import com.jadaptive.api.json.BootstrapTablePageProcessor;
import com.jadaptive.api.json.BootstrapTableResult;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.templates.TemplateVersion;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.app.entity.MongoEntity;

@Controller
public class APIController extends BootstrapTableController<MongoEntity>{

	static Logger log = LoggerFactory.getLogger(APIController.class);
	
	@Autowired
	private EntityTemplateService templateService; 
	
	@Autowired
	private TemplateVersionService versionService; 
	
	@Autowired
	private EntityService<MongoEntity> entityService; 
	
	@ExceptionHandler(AccessDeniedException.class)
	public void handleException(HttpServletRequest request, 
			HttpServletResponse response,
			AccessDeniedException e) throws IOException {
	   response.sendError(HttpStatus.FORBIDDEN.value(), e.getMessage());
	}
	
	@RequestMapping(value="api/template/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<EntityTemplate> doEntityGet(@PathVariable String resourceKey, HttpServletRequest request) throws RepositoryException, UnknownEntityException, EntityException {
		
		try {
		   return new EntityStatus<EntityTemplate>(templateService.get(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/{}", resourceKey, e);
			}
			return new EntityStatus<EntityTemplate>(false, e.getMessage());
		} 
	}
	
	@RequestMapping(value="api/template", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveTemplate(@RequestBody EntityTemplate template, HttpServletRequest request) {

		try {
			templateService.saveOrUpdate(template);
			return new RequestStatus();
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/template", e);
			}
			return new RequestStatus(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/template/{uuid}", method = RequestMethod.DELETE, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus deleteTemplate(@PathVariable String uuid, HttpServletRequest request) {

		try {
			templateService.delete(uuid);
			return new RequestStatus();
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("DELETE api/template", e);
			}
			return new RequestStatus(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/template/versions", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<Collection<TemplateVersion>> getTemplateVersions(HttpServletRequest request) throws RepositoryException, UnknownEntityException, EntityException {

		try {
		   return new EntityStatus<Collection<TemplateVersion>>(versionService.list());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/versions", e);
			}
			return new EntityStatus<Collection<TemplateVersion>>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/template/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<Collection<EntityTemplate>> getEntityTemplates(HttpServletRequest request) throws RepositoryException, UnknownEntityException, EntityException {
		try {
		   return new EntityStatus<Collection<EntityTemplate>>(templateService.list());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/list", e);
			}
			return new EntityStatus<Collection<EntityTemplate>>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/template/table", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public TableStatus<EntityTemplate> getTemplateTable(HttpServletRequest request,
			@RequestParam(required=false, defaultValue="uuid") String searchField,
			@RequestParam(required=false, name="search") String searchValue,
			@RequestParam(required=false, defaultValue = "asc") String order,
			@RequestParam int offset,
			@RequestParam int limit) throws RepositoryException, UnknownEntityException, EntityException {
		try {

		   return new TableStatus<EntityTemplate>(templateService.table(searchField, searchValue, order, offset, limit), templateService.count());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/list", e);
			}
			return new TableStatus<EntityTemplate>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/{resourceKey}/{uuid}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<MongoEntity> getEntity(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, EntityException {
		try {
		   return new EntityStatus<MongoEntity>(entityService.get(resourceKey, uuid));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/{}", resourceKey, uuid, e);
			}
			return new EntityStatus<MongoEntity>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<MongoEntity> getEntity(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, EntityException {
		
		if(resourceKey.equals("logon")) {
			return new EntityStatus<MongoEntity>(false, "Logon API requires POST request");
		}
		
		try {
			   return new EntityStatus<MongoEntity>(entityService.getSingleton(resourceKey));
		} catch(Throwable e) {
			return handleException(e, "GET", resourceKey);
		}
	}
	
	private EntityStatus<MongoEntity> handleException(Throwable e, String method, String resourceKey) {
		if(e instanceof AccessDeniedException) {
			throw (AccessDeniedException)e;
		}
		if(log.isErrorEnabled()) {
			log.error("{} api/{}", method, resourceKey, e);
		}
		return new EntityStatus<MongoEntity>(false, e.getMessage());
	}
	
	@RequestMapping(value="api/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveEntity(HttpServletRequest request, @PathVariable String resourceKey, @RequestBody MongoEntity entity)  {

		try {
			entityService.saveOrUpdate(entity);
			return new RequestStatus();
		} catch (Throwable e) {
			return handleException(e, "POST", resourceKey);
		}
	}
	
	@RequestMapping(value="api/{resourceKey}/{uuid}", method = RequestMethod.DELETE, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveEntity(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, EntityException {

		try {
			entityService.delete(resourceKey, uuid);
			return new RequestStatus();
		} catch (Throwable e) {
			return handleException(e, "DELETE", resourceKey);
		}
	}
	
	@RequestMapping(value="api/{resourceKey}/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<Collection<MongoEntity>> listEntities(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, EntityException {
		try {
			   return new EntityStatus<Collection<MongoEntity>>(entityService.list(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/list", resourceKey, e);
			}
			return new EntityStatus<Collection<MongoEntity>>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/{resourceKey}/table", method = { RequestMethod.POST, RequestMethod.GET }, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public BootstrapTableResult<MongoEntity> tableEntities(HttpServletRequest request, 
			@PathVariable String resourceKey,
			@RequestParam(required=false, defaultValue="uuid") String searchField,
			@RequestParam(required=false, name="search") String searchValue,
			@RequestParam(required=false, defaultValue = "") String orderColumn,
			@RequestParam(required=false, defaultValue = "asc") String order,
			@RequestParam(required=false, defaultValue = "0") int offset,
			@RequestParam(required=false, defaultValue = "100") int limit) throws RepositoryException, UnknownEntityException, EntityException {
		
		try {
			
			if(StringUtils.isBlank(orderColumn)) {
				orderColumn = searchField;
			}
			
			return processDataTablesRequest(request, 
					templateService.get(resourceKey),
				new BootstrapTablePageProcessor() {

					@Override
					public Collection<?> getPage(String searchColumn, String searchPattern, int start,
							int length, String sortBy)
							throws UnauthorizedException,
							AccessDeniedException {
						return entityService.table(resourceKey, searchField, searchValue, offset, limit);
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return entityService.count(resourceKey, searchField, searchValue);
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
