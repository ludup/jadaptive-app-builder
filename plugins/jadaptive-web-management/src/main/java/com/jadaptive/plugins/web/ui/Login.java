package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.auth.AuthenticationPolicyService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.User;
import com.jadaptive.plugins.web.ui.Login.LoginForm;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Login extends AuthenticationPage<LoginForm> {

	static Logger log = LoggerFactory.getLogger(Login.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private AuthenticationPolicyService policyService;
	
	public Login() {
		super(LoginForm.class);
	}

	@Override
	protected void generateContent(Document doc) throws FileNotFoundException {
		if(tenantService.isSetupMode()) {
			throw new UriRedirect("/app/ui/wizards/setup");
		}
		
		AuthenticationState state = authenticationService.getCurrentState();
		if(!state.getCurrentPage().equals(Login.class)) {
			authenticationService.clearAuthenticationState();
		}
		
		super.generateContent(doc);
	}
	
	protected boolean doForm(Document document, AuthenticationState state, LoginForm form) {
		
		try {
			state.setAttemptedUsername(form.getUsername());
			User user = userService.getUser(form.getUsername());
			state.setUser(user);
			
			authenticationService.processRequiredAuthentication(state, policyService.getAssignedPolicy(user));
			return true;
    	
    	} catch(AccessDeniedException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		Feedback.error(e.getMessage());
    	} catch(ObjectNotFoundException e) {
    	} catch(Throwable e) {
    		log.error("Error in login", e);
    	}
    	
    	Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    	Feedback.error("default", "error.invalidCredentials");

		return false;
	}
	
	@Override
	public String getUri() {
		return "login";
	}
	
	public interface LoginForm {
		String getUsername();
	}
}
