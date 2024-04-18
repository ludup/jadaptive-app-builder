package com.jadaptive.app.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.lang.model.UnknownEntityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.json.BootstrapTableController;
import com.jadaptive.api.json.BootstrapTablePageProcessor;
import com.jadaptive.api.json.BootstrapTableResult;
import com.jadaptive.api.json.ResourceList;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.NamePairValue;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PermissionController extends BootstrapTableController<NamePairValue> {

	static Logger log = LoggerFactory.getLogger(ObjectsJsonController.class);
	
	@Autowired
	PermissionService permissionService; 
		
	@RequestMapping(value="/app/api/permissions/list", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public ResourceList<NamePairValue> getEntityTemplates(HttpServletRequest request) throws RepositoryException, UnknownEntityException, ObjectException {
		try {
		   return new ResourceList<NamePairValue>(permissionService.getPermissions());
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/permissions/list", e);
			}
			return new ResourceList<NamePairValue>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/permissions/table", method = { RequestMethod.POST, RequestMethod.GET }, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public BootstrapTableResult<NamePairValue> tablePermissions(HttpServletRequest request, 
			@RequestParam(required=false, defaultValue = "asc") String order,
			@RequestParam(required=false, defaultValue = "0") int offset,
			@RequestParam(required=false, defaultValue = "100") int limit) throws RepositoryException, UnknownEntityException, ObjectException {
		
		try {
			
			ThreadLocal<List<NamePairValue>> results = new ThreadLocal<>();
			
			return processDataTablesRequest(request, 
				new BootstrapTablePageProcessor() {

					@Override
					public Collection<?> getPage(String searchColumn, String searchPattern, int start,
							int length, String sortBy)
							throws UnauthorizedException,
							AccessDeniedException {
						Collection<NamePairValue> cons = permissionService.getPermissions();
						List<NamePairValue> tmp = new ArrayList<>();
						for(NamePairValue con : cons) {
							if(con.getName().startsWith(searchPattern)) {
								tmp.add(con);
							}
						}

						length = Math.min(tmp.size() - start, length);
						
						results.set(new ArrayList<>(tmp));
						tmp = tmp.subList(start, length);

						return tmp;
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return (long) results.get().size();
					}
				});

		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/permissions/table", e);
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
