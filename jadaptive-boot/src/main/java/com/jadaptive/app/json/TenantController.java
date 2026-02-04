package com.jadaptive.app.json;

import java.util.Objects;

import javax.lang.model.UnknownEntityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.json.EntityResultsStatus;
import com.jadaptive.api.json.EntityStatus;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class TenantController {

	static Logger log = LoggerFactory.getLogger(ObjectsJsonController.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@RequestMapping(value="/app/api/tenant/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityResultsStatus<Tenant> getEntityTemplates(HttpServletRequest request) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
		   return new EntityResultsStatus<Tenant>(tenantService.allObjects());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/tenant/list", e);
			}
			return new EntityResultsStatus<Tenant>(false, e.getMessage());
		}
	}
	
	
	@RequestMapping(value="/app/api/tenant/validate/{domain}/", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<Boolean> validateDomain(HttpServletRequest request, @PathVariable String domain) throws RepositoryException, UnknownEntityException, ObjectException {
		
		try {
			tenantService.getTenantByDomain(domain);
			return new EntityStatus<>(Boolean.FALSE);
		} catch(ObjectNotFoundException e) {
			return new EntityStatus<>(Boolean.TRUE);
		}
		
	}
	
	@RequestMapping(value="/app/api/tenant/scheduleDelete/{uuid}", method = RequestMethod.DELETE)
	public void scheduleDelete(HttpServletRequest request, @PathVariable String uuid) throws RepositoryException, UnknownEntityException, ObjectException {
		
		try {
			Tenant tenant = tenantService.getTenantByUUID(uuid);
			if(Objects.nonNull(tenant.getDeletionDate())) {
				Feedback.info(Tenant.RESOURCE_KEY, "deletion.alreadyScheduled", 
						tenant.getName(), 
						Utils.formatDate(tenant.getDeletionDate()));
			} else {
				tenantService.scheduleDelete(tenant);
				Feedback.info(Tenant.RESOURCE_KEY, "deletion.scheduled", tenant.getName());
			}
		} catch(Throwable e) {
			Feedback.error(e.getMessage());
		}
		
	}
}
