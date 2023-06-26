package com.jadaptive.api.ui.pages.user;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.AuthenticationScope;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.auth.PostAuthenticatorPage;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.pages.user.ChangePassword.PasswordForm;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@ModalPage
public class ChangePassword extends AuthenticationPage<PasswordForm> implements PostAuthenticatorPage {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private TenantService tenantService; 
	
	public ChangePassword() {
		super(PasswordForm.class);
	}

	@Override
	public String getUri() {
		return "change-password";
	}

	public boolean doForm(Document document, AuthenticationState state, PasswordForm form) throws AccessDeniedException {

		return tenantService.execute(()->{
			permissionService.setupUserContext(state.getUser());
			try {
				User user = state.getUser();
				userService.changePassword(user, form.getPassword().toCharArray(), false);
				return true;
			} finally {
				permissionService.clearUserContext();
			}
		});
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
	public AuthenticationScope getScope() {
		return AuthenticationScope.USER_LOGIN;
	}

	@Override
	public boolean requiresProcessing(AuthenticationState state) {
		if(state.getUser() instanceof PasswordEnabledUser) {
			
	    	if(((PasswordEnabledUser)state.getUser()).getPasswordChangeRequired()) {
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
		return false;
	}
}
