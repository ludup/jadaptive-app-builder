package com.jadaptive.json;

import java.util.Collection;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantService;

@Controller
public class TenantController {

	static Logger log = LoggerFactory.getLogger(APIController.class);
	
	@Autowired
	TenantService tenantService; 
	
	@RequestMapping(value="api/tenant/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public EntityStatus<Collection<Tenant>> getEntityTemplates(HttpServletRequest request) throws RepositoryException, UnknownEntityException, EntityException {
		try {
		   return new EntityStatus<Collection<Tenant>>(tenantService.getTenants());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/tenant/list", e);
			}
			return new EntityStatus<Collection<Tenant>>(false, e.getMessage());
		}
	}

}
