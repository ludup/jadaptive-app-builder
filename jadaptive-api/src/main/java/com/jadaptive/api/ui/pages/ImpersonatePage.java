package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequestPage(path = "impersonate/{uuid}")
public class ImpersonatePage extends AuthenticatedPage {

	@Autowired
	private TenantService tenantService;

	@Autowired
	private SessionService sessionService; 

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private PageCache pageCache;
	
	@Override
	public String getUri() {
		return "impersonate";
	}
	
	String uuid;

	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		
		try {
			Session session = Session.get(request);
			if(session.isImpersontating()) {
				throw new IllegalStateException("The session is already impersonating in " + session.getImpersontatingTenant().getDomain());
			}
			if(!session.getTenant().isSystem()) {
				throw new IllegalStateException("You cannot impersonate from a non-system tenant!");
			}

			Tenant tenant = tenantService.getTenantByUUID(uuid);
			
			sessionService.impersonate(tenant, session);
			try(var uc = permissionService.userContext(session.getUser())) {
				Feedback.success("userInterface", "impersonate.success", tenant.getName());
				throw new PageRedirect(pageCache.getHomePage());
			}
		} catch(Redirect r) {
			throw r;
		}catch(Throwable e) {
			Feedback.error(e.getMessage());
			throw new UriRedirect("/app/ui/search/tenant");
		}
		
	}

}
