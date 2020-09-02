package com.jadaptive.app.ui;

import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Bound;
import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.Created;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.Request;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bindable.FormBindable.AutoCompleteMode;
import com.codesmith.webbits.bindable.FormBindable.FormField;
import com.codesmith.webbits.bindable.FormBindable.InputRestriction;
import com.codesmith.webbits.widgets.Feedback;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPage;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Page
@View(contentType = "text/html", paths = { "/changePassword" })
@ClasspathResource
public class ChangePassword extends AbstractPage {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private UserService userService;

	@Autowired
	private SessionUtils sessionUtils;

	@Bound
	private PasswordForm changePasswordForm;

	@Created
	void created(Request<?> request) {
		if (!sessionUtils.hasActiveSession(request)) {
			/*
			 * If not authenticated, we may be in the change password state from login (dont
			 * have an active session at this point), so make sure we are in an
			 * authenticating state, otherwise redirect to where we should be.
			 */
			AuthenticationState state = authenticationService.getCurrentState();
			if (!state.getCurrentPage().equals(this.getClass())) {
				throw new Redirect(state.getCurrentPage());
			}
			if (!state.hasUser())
				throw new Redirect(authenticationService.resetAuthentication());
		}
	}

	@Bound
	void changePasswordForm() {
		/* Validate */
		if (!changePasswordForm.getPassword().equals(changePasswordForm.getConfirmPassword())) {
			changePasswordForm.getFeedback().error("The passwords do not match.");
			return;
		}

		Session session = sessionUtils.getActiveSession(Request.get());
		if (session == null) {
			/* For a change password during authentication */
			AuthenticationState state = authenticationService.getCurrentState();
			if (!state.getCurrentPage().equals(this.getClass())) {
				throw new Redirect(state.getCurrentPage());
			}
			if (!state.hasUser())
				throw new Redirect(authenticationService.resetAuthentication());
			permissionService.runAs(state.getUser(), () -> userService.changePassword(state.getUser(),
					changePasswordForm.getPassword().toCharArray(), false));
			throw new Redirect(authenticationService.completeAuthentication(state.getUser()));
		} else {
			User user = session.getUser();
			userService.changePassword(user, changePasswordForm.getPassword().toCharArray(), false);
			throw new Redirect(authenticationService.completeAuthentication(user));

		}
	}
	
	@Bound
    public boolean isLoggedOn() {
		return sessionUtils.hasActiveSession(Request.get());
	}

	public boolean isModal() {
		return true;
	}

	public interface PasswordForm {

		@FormField(type = "password", autoComplete = AutoCompleteMode.NEW_PASSWORD)
		@InputRestriction(required = true)
		String getPassword();

		@FormField(type = "password", autoComplete = AutoCompleteMode.NEW_PASSWORD)
		@InputRestriction(required = true)
		String getConfirmPassword();

		Feedback getFeedback();
	}

}
