package com.jadaptive.json;

import java.util.Collection;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;

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

import com.jadaptive.entity.Entity;
import com.jadaptive.entity.EntityException;
import com.jadaptive.entity.EntityService;
import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.EntityTemplateService;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.templates.TemplateVersion;
import com.jadaptive.templates.TemplateVersionService;

@Controller
public class APIController {

	static Logger log = LoggerFactory.getLogger(APIController.class);
	
	@Autowired
	EntityTemplateService templateService; 
	
	@Autowired
	TemplateVersionService versionService; 
	
	@Autowired
	EntityService entityService;
	
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
			@RequestParam(required=false) String search,
			@RequestParam String order,
			@RequestParam int offset,
			@RequestParam int limit) throws RepositoryException, UnknownEntityException, EntityException {
		try {

		   return new TableStatus<EntityTemplate>(templateService.table(search, order, offset, limit), templateService.count());
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
	public EntityStatus<Entity> getEntity(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, EntityException {
		try {
		   return new EntityStatus<Entity>(entityService.get(resourceKey, uuid));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/{}", resourceKey, uuid, e);
			}
			return new EntityStatus<Entity>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<Entity> getEntity(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, EntityException {
		try {
			   return new EntityStatus<Entity>(entityService.getSingleton(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}", resourceKey, e);
			}
			return new EntityStatus<Entity>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveEntity(HttpServletRequest request, @PathVariable String resourceKey, @RequestBody Entity entity)  {

		try {
			entityService.saveOrUpdate(entity);
			return new RequestStatus();
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/{}", resourceKey, e);
			}
			return new RequestStatus(false, e.getMessage());
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
			if(log.isErrorEnabled()) {
				log.error("DELETE api/{}/{}", resourceKey, uuid, e);
			}
			return new RequestStatus(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/{resourceKey}/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<Collection<Entity>> listEntities(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, EntityException {
		try {
			   return new EntityStatus<Collection<Entity>>(entityService.list(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/list", resourceKey, e);
			}
			return new EntityStatus<Collection<Entity>>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/{resourceKey}/table", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityTableStatus<Entity> tableEntities(HttpServletRequest request, 
			@PathVariable String resourceKey,
			@RequestParam(required=false) String search,
			@RequestParam String order,
			@RequestParam int offset,
			@RequestParam int limit) throws RepositoryException, UnknownEntityException, EntityException {
		try {
			   return new EntityTableStatus<Entity>(templateService.get(resourceKey), 
					   entityService.table(resourceKey, offset, limit),
					   entityService.count(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/{}/table", resourceKey, e);
			}
			return new EntityTableStatus<Entity>(false, e.getMessage());
		}
	}
}
