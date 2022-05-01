package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.util.Objects;

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
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.web.ui.Password.LoginForm;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Password extends AuthenticationPage<LoginForm> {

	static Logger log = LoggerFactory.getLogger(Password.class);
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private PageCache pageCache; 
	
	public Password() {
		super(LoginForm.class);
	}

	@Override
	protected void generateContent(Document doc) throws FileNotFoundException {

		
		AuthenticationState state = authenticationService.getCurrentState();
		
		if(!state.getCurrentPage().equals(Password.class)) {
			authenticationService.clearAuthenticationState();
			throw new PageRedirect(pageCache.resolvePage(Login.class));
		}
		
		if(Objects.isNull(state.getUser()) ) {
			throw new PageRedirect(pageCache.resolvePage(Login.class));
		}
		
		doc.selectFirst("#username").val(state.getUser().getUsername());
		
		super.generateContent(doc);
	}
	
	protected boolean doForm(Document document, AuthenticationState state, LoginForm form) {
		
		try {

			User user = state.getUser();
			if(userService.verifyPassword(state.getUser(), form.getPassword().toCharArray())) {
				
				permissionService.setupUserContext(user);
				
				try {
					if(user instanceof PasswordEnabledUser) {
						
				    	if(((PasswordEnabledUser)user).getPasswordChangeRequired()) {
				    		try {
					    		permissionService.assertPermission(UserService.CHANGE_PASSWORD_PERMISSION);
								state.getPostAuthenticationPages().add(ChangePassword.class);
				    		} catch(AccessDeniedException e) { }
				    	}
			    	}
					
					state.setAttribute(AuthenticationService.PASSWORD, form.getPassword());
					return true;
				
				} finally {
					permissionService.clearUserContext();
				}
			}
			
			authenticationService.reportAuthenticationFailure(state);
    	
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
		return "password";
	}
	
	public interface LoginForm {
		String getPassword();
	}
}
