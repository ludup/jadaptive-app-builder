package com.jadaptive.app.json;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;

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
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;

@Controller
public class TenantController {

	static Logger log = LoggerFactory.getLogger(ObjectsJsonController.class);
	
	@Autowired
	TenantService tenantService; 
		
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

}
