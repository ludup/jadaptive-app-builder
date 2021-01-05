package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.app.ui.Login.LoginForm;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
public class Login extends AuthenticationPage<LoginForm> {

	public Login() {
		super(LoginForm.class);
	}

	protected boolean doForm(Document document, AuthenticationState state, LoginForm form) {
		
		try {

			state.setAttemptedUsername(form.getUsername());
			User user = userService.getUser(form.getUsername());
	    	
			if(userService.verifyPassword(user, form.getPassword().toCharArray())) {
				
				state.setUser(user);
				if(user instanceof PasswordEnabledUser) {
					
			    	if(((PasswordEnabledUser)user).getPasswordChangeRequired()) {
						state.getPostAuthenticationPages().add(ChangePassword.class);
			    	}
		    	}
				
				state.setAttribute(AuthenticationService.PASSWORD, form.getPassword());
				return true;
			}
			
			authenticationService.reportAuthenticationFailure(state);
    	
    	} catch(AccessDeniedException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		document.selectFirst("#feedback").append("<div class=\"alert alert-danger\">" + e.getMessage() + "</div>");
    	} catch(ObjectNotFoundException e) {
    	}
    	
    	Request.response().setStatus(HttpStatus.FORBIDDEN.value());
		document.selectFirst("#feedback").append("<div class=\"alert alert-danger\">Invalid credentials</div>");
		return false;
	}
	
	@Override
	public String getUri() {
		return "login";
	}
	
	public interface LoginForm {
		String getUsername();
		String getPassword();
	}
}
