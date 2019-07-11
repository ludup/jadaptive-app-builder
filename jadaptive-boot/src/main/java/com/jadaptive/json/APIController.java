package com.jadaptive.json;

import java.util.Collection;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.entity.Entity;
import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.entity.EntityService;
import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.EntityTemplateService;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.templates.Template;
import com.jadaptive.templates.TemplateService;

@Controller
public class APIController {

	@Autowired
	EntityTemplateService templateService; 
	
	@Autowired
	TemplateService versionService; 
	
	@Autowired
	EntityService entityService;
	
	@RequestMapping(value="api/template/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Object doEntityGet(@PathVariable String resourceKey, HttpServletRequest request) throws RepositoryException, UnknownEntityException, EntityNotFoundException {

		return templateService.get(resourceKey);
	}
	
	@RequestMapping(value="api/template", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveTemplate(@RequestBody EntityTemplate template, HttpServletRequest request) {

		try {
			templateService.saveOrUpdate(template);
			return new RequestStatus();
		} catch (RepositoryException e) {
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
		} catch (RepositoryException | EntityNotFoundException e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="api/template/versions", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Collection<Template> getTemplateVersions(HttpServletRequest request) throws RepositoryException, UnknownEntityException, EntityNotFoundException {

		return versionService.list();
	}
	
	@RequestMapping(value="api/template/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Collection<EntityTemplate> getEntityTemplates(HttpServletRequest request) throws RepositoryException, UnknownEntityException, EntityNotFoundException {

		return templateService.list();
	}
	
	
	@RequestMapping(value="api/{resourceKey}/{uuid}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Entity getEntity(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, EntityNotFoundException {

		return entityService.get(resourceKey, uuid);
	}
	
	@RequestMapping(value="api/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Entity getEntity(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, EntityNotFoundException {

		return entityService.getSingleton(resourceKey);
	}
	
	@RequestMapping(value="api/{resourceKey}", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveEntity(HttpServletRequest request, @PathVariable String resourceKey, @RequestBody Entity entity)  {

		try {
			entityService.saveOrUpdate(resourceKey, entity);
			return new RequestStatus();
		} catch (RepositoryException | EntityNotFoundException e) {
			return new RequestStatus(false, e.getMessage());
		}
		
		
	}
	
	@RequestMapping(value="api/{resourceKey}/{uuid}", method = RequestMethod.DELETE, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveEntity(HttpServletRequest request, @PathVariable String resourceKey, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, EntityNotFoundException {

		try {
			entityService.delete(resourceKey, uuid);
			return new RequestStatus();
		} catch (RepositoryException | EntityNotFoundException e) {
			return new RequestStatus(false, e.getMessage());
		}
		

	}
	
	@RequestMapping(value="api/{resourceKey}/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Collection<Entity> listEntities(HttpServletRequest request, @PathVariable String resourceKey) throws RepositoryException, UnknownEntityException, EntityNotFoundException {
		return entityService.list(resourceKey);
	}
}
