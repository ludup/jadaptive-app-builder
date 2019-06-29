package com.jadaptive.json;

import java.util.Collection;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.entity.Entity;
import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.entity.EntityService;
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
	
	@RequestMapping(value="api/template/versions", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public Collection<Template> getTemplateVersions(HttpServletRequest request) throws RepositoryException, UnknownEntityException, EntityNotFoundException {

		return versionService.list();
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

		return entityService.get(resourceKey);
	}
}
