package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.Created;
import com.codesmith.webbits.Form;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPage;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Page
@View(contentType = "text/html", paths = { "login"})
@ClasspathResource
public class Login extends AbstractPage {
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private SessionUtils sessionUtils; 
	
	@Autowired
	private UserService userService; 
	
	@Created
	void created() throws FileNotFoundException {
		if(sessionUtils.hasActiveSession(Request.get())) {
			throw new Redirect(JadaptiveApp.class);
		}
		AuthenticationState state = authenticationService.getCurrentState();
		if(!state.getCurrentPage().equals(this.getClass())) {
			throw new Redirect(state.getCurrentPage());
		}
	}		
	    		
	 @Out(methods = HTTPMethod.GET)
	 Document get(@In Document content) {
	 
		 authenticationService.decorateAuthenticationPage(content);
		 return content;
	 }
	 
    @Out(methods = HTTPMethod.POST)
    Document post(@In Document content, @Form LoginForm form) {
	
    	try {
    		
			if(!Boolean.getBoolean("jadaptive.webUI")) {
				throw new AccessDeniedException("Web UI is currently disabled. Login to manage your account via the SSH CLI");
			}
			
			AuthenticationState state = authenticationService.getCurrentState();
			if(state.hasUser()) {
				content.selectFirst("#username").val(state.getUser().getUsername());
				content.selectFirst("#username").attr("readonly", "true");
			}
			 
			User user = userService.getUser(form.getUsername());
	    	
			if(userService.verifyPassword(user, form.getPassword().toCharArray())) {
				
				if(user instanceof PasswordEnabledUser) {
					
			    	if(((PasswordEnabledUser)user).getPasswordChangeRequired()) {
						state.getAuthenticationPages().add(ChangePassword.class);
			    	}
		    	}
				
				state.setAttribute(AuthenticationService.PASSWORD, form.getPassword());
				throw new Redirect(authenticationService.completeAuthentication(user));
			}
			
			authenticationService.reportAuthenticationFailure(form.getUsername());
    	
    	} catch(AccessDeniedException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		content.selectFirst("#feedback").append("<div class=\"alert alert-danger\">" + e.getMessage() + "</div>");
    		return content;
    	} catch(ObjectNotFoundException e) {
    		
    	}
    	
    	Request.response().setStatus(HttpStatus.FORBIDDEN.value());
		content.selectFirst("#feedback").append("<div class=\"alert alert-danger\">Bad username or password</div>");
		return content;
    }

    public interface LoginForm {
		String getUsername();
		String getPassword();
    }
}
