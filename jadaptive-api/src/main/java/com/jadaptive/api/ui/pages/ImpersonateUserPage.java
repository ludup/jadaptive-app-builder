package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequestPage(path = "impersonateUser/{uuid}")
public class ImpersonateUserPage extends AuthenticatedPage {

	@Autowired
	private SessionService sessionService; 

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private UserService userService; 
	
	@Override
	public String getUri() {
		return "impersonateUser";
	}
	
	String uuid;

	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		
		try {
			Session session = Session.get(request);

			User user = userService.getUserByUUID(uuid);
			sessionService.impersonate(user, session);
			try(var uc = permissionService.userContext(session.getUser())) {
				Feedback.success("userInterface", "impersonateUser.success", user.getName());
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
