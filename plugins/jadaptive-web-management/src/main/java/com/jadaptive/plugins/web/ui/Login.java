package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.plugins.web.ui.Login.LoginForm;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Login extends AuthenticationPage<LoginForm> {

	static Logger log = LoggerFactory.getLogger(Login.class);
	
	@Autowired
	private TenantService tenantService; 
	
	public Login() {
		super(LoginForm.class);
	}

	@Override
	protected void generateContent(Document doc) {
		if(tenantService.isSetupMode()) {
			throw new UriRedirect("/app/ui/wizards/setup");
		}
		super.generateContent(doc);
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
    	} catch(Throwable e) {
    		log.error("Error in login", e);
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
