package com.jadaptive.app.json;

import java.io.IOException;

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

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.templates.TemplateVersion;
import com.jadaptive.api.templates.TemplateVersionService;

@Controller
public class TemplateController {

	static Logger log = LoggerFactory.getLogger(ObjectController.class);
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private TemplateVersionService versionService;
	
	@ExceptionHandler(AccessDeniedException.class)
	public void handleException(HttpServletRequest request, 
			HttpServletResponse response,
			AccessDeniedException e) throws IOException {
	   response.sendError(HttpStatus.FORBIDDEN.value(), e.getMessage());
	}
	
	@RequestMapping(value="/app/api/template/{resourceKey}", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<ObjectTemplate> doEntityGet(@PathVariable String resourceKey, HttpServletRequest request) throws RepositoryException, UnknownEntityException, ObjectException {
		
		try {
		   return new EntityStatus<ObjectTemplate>(templateService.get(resourceKey));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/{}", resourceKey, e);
			}
			return new EntityStatus<ObjectTemplate>(false, e.getMessage());
		} 
	}
	
	@RequestMapping(value="/app/api/template", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus saveTemplate(@RequestBody ObjectTemplate template, HttpServletRequest request) {

		try {
			templateService.saveOrUpdate(template);
			return new RequestStatusImpl();
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/template", e);
			}
			return new RequestStatusImpl(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/template/{uuid}", method = RequestMethod.DELETE, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus deleteTemplate(@PathVariable String uuid, HttpServletRequest request) {

		try {
			templateService.delete(uuid);
			return new RequestStatusImpl();
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("DELETE api/template", e);
			}
			return new RequestStatusImpl(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/template/versions", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityResultsStatus<TemplateVersion> getTemplateVersions(HttpServletRequest request) throws RepositoryException, UnknownEntityException, ObjectException {

		try {
		   return new EntityResultsStatus<TemplateVersion>(versionService.list());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/versions", e);
			}
			return new EntityResultsStatus<TemplateVersion>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/template/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityResultsStatus<ObjectTemplate> getEntityTemplates(HttpServletRequest request) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
		   return new EntityResultsStatus<ObjectTemplate>(templateService.list());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/list", e);
			}
			return new EntityResultsStatus<ObjectTemplate>(false, e.getMessage());
		}
	}
	
	
	@RequestMapping(value="/app/api/template/{uuid}/children", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityResultsStatus<ObjectTemplate> getChildTemplates(HttpServletRequest request, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
		   return new EntityResultsStatus<ObjectTemplate>(templateService.children(uuid));
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/{}/children", uuid, e);
			}
			return new EntityResultsStatus<ObjectTemplate>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/template/table", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public TableStatus<ObjectTemplate> getTemplateTable(HttpServletRequest request,
			@RequestParam(required=false, defaultValue="uuid") String searchField,
			@RequestParam(required=false, name="search") String searchValue,
			@RequestParam(required=false, defaultValue = "asc") String order,
			@RequestParam int offset,
			@RequestParam int limit) throws RepositoryException, UnknownEntityException, ObjectException {
		try {

		   return new TableStatus<ObjectTemplate>(templateService.table(searchField, searchValue, order, offset, limit), templateService.count());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/template/table", e);
			}
			return new TableStatus<ObjectTemplate>(false, e.getMessage());
		}
	}
}
