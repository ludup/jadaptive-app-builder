package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.codesmith.webbits.Created;
import com.codesmith.webbits.Form;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AbstractPage;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.app.auth.AuthenticationService;

@Page
@View(contentType = "text/html", paths = { "/login"})
@Resource
public class Login extends AbstractPage {
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionUtils sessionUtils; 
	
	@Created
	void created() throws FileNotFoundException {
		if(sessionUtils.hasActiveSession(Request.get())) {
			throw new Redirect(Dashboard.class);
		}
	}
	
    @Out(methods = HTTPMethod.POST)
    Document service(@In Document content, @Form LoginForm form) {
	
    	try {
    		
			if(!Boolean.getBoolean("jadaptive.webUI")) {
				throw new AccessDeniedException("Web UI is currently disabled. Login to manage your account via the SSH CLI");
			}
			
	    	Session session = authenticationService.logonUser(form.getUsername(),
	    			form.getPassword(), tenantService.getCurrentTenant(), 
	    			Request.get().getRemoteAddr(), 
	    			Request.get().getHeader(HttpHeaders.USER_AGENT));
	    	
	    	sessionUtils.addSessionCookies(Request.get(), Request.response(), session);
	    	
	    	User user = session.getUser();
	    	if(user instanceof PasswordEnabledUser) {
		    	if(((PasswordEnabledUser)user).getPasswordChangeRequired()) {
		    		throw new Redirect(ChangePassword.class);
		    	}
	    	}
	    	
	    	throw new Redirect(JadaptiveApp.class);
    	
    	} catch(AccessDeniedException | ObjectNotFoundException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		content.selectFirst("#feedback").append("<div class=\"alert alert-danger\">" + e.getMessage() + "</div>");
    		return content;
    	}
    }

    public interface LoginForm {
		String getUsername();
		String getPassword();
    }
}
