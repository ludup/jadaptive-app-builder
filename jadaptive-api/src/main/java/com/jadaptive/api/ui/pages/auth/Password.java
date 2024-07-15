package com.jadaptive.api.ui.pages.auth;

import java.io.FileNotFoundException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.auth.AuthenticatorPage;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.pages.auth.Password.LoginForm;
import com.jadaptive.api.user.PasswordEnabledUser;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Password extends AuthenticationPage<LoginForm> implements AuthenticatorPage {

	static Logger log = LoggerFactory.getLogger(Password.class);
	
	@Autowired
	private PageCache pageCache; 
	
	public Password() {
		super(LoginForm.class);
	}

	@Override
	protected void doGenerateContent(Document doc) throws FileNotFoundException {

		
		AuthenticationState state = authenticationService.getCurrentState();
		
		if(!Password.class.equals(state.getCurrentPage().orElse(null))) {
			authenticationService.clearAuthenticationState();
			throw new PageRedirect(pageCache.resolvePage(Login.class));
		}
		
		if(Objects.isNull(state.getUser()) ) {
			throw new PageRedirect(pageCache.resolvePage(Login.class));
		}
		
		doc.selectFirst("#username").val(state.getUser().getUsername());
	}
	
	@Override
	public String getBundle() {
		return "userInterface";
	}
	
	protected boolean doForm(Document document, AuthenticationState state, LoginForm form) {
		
		try {

			if(userService.verifyPassword(state.getUser(), form.getPassword().toCharArray())) {
				state.setAttribute(AuthenticationService.PASSWORD, form.getPassword());
				return true;
			}

    	} catch(AccessDeniedException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		Feedback.error(e.getMessage());
    		log.error("Access Denied", e);
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
		return "password";
	}
	
	public interface LoginForm {
		String getPassword();
	}

	@Override
	public boolean canAuthenticate(AuthenticationState state) {
		return state.getUser() instanceof PasswordEnabledUser;
	}

	@Override
	public String getAuthenticatorUUID() {
		return AuthenticationService.PASSWORD_MODULE_UUID;
	}
}
