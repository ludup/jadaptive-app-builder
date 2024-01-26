package com.jadaptive.api.ui.pages.user;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.auth.PostAuthenticatorPage;
import com.jadaptive.api.auth.UserLoginAuthenticationPolicy;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.pages.user.ChangePassword.PasswordForm;
import com.jadaptive.api.user.PasswordChangeRequired;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@ModalPage
public class ChangePassword extends AuthenticationPage<PasswordForm> implements PostAuthenticatorPage {

	@Autowired
	private PermissionService permissionService; 
		
	public ChangePassword() {
		super(PasswordForm.class);
	}

	@Override
	public String getUri() {
		return "change-password";
	}

	public boolean doForm(Document document, AuthenticationState state, PasswordForm form) throws AccessDeniedException {

		
		permissionService.setupUserContext(state.getUser());
		try {
			User user = state.getUser();
			userService.changePassword(user, form.getPassword().toCharArray(), false);
			state.setAttribute(AuthenticationService.PASSWORD, form.getPassword());
			return true;
		} catch(Throwable e) { 
			Feedback.error(e.getMessage());
		} finally {
			permissionService.clearUserContext();
		}
		
		return false;
		
	}

	public interface PasswordForm {

		String getPassword();
		String getConfirmPasssord();
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public boolean requiresProcessing(AuthenticationState state) {
		
		if(state.getPolicy().getResourceKey().equals(UserLoginAuthenticationPolicy.RESOURCE_KEY)) {
			if(state.getUser() instanceof PasswordChangeRequired) {
				
		    	if(((PasswordChangeRequired)state.getUser()).getPasswordChangeRequired()) {
		    		permissionService.setupUserContext(state.getUser());
		    		try {
			    		permissionService.assertPermission(UserService.CHANGE_PASSWORD_PERMISSION);
						return true;
		    		} catch(AccessDeniedException e) {
		    			
		    		}
		    		finally {
		    			permissionService.clearUserContext();
		    		}
		    	}
	    	}
		}
		return false;
	}
}
