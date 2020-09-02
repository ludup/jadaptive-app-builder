package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Bound;
import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.Created;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.Request;
import com.codesmith.webbits.Response;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bindable.FormBindable.FormField;
import com.codesmith.webbits.bindable.FormBindable.InputRestriction;
import com.codesmith.webbits.extensions.Enablement;
import com.codesmith.webbits.widgets.Feedback;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPage;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Page(Enablement.class)
@View(contentType = "text/html", paths = { "login" })
@ClasspathResource
public class Login extends AbstractPage {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private SessionUtils sessionUtils;

	@Autowired
	private UserService userService;

	@Bound
	private LoginForm loginForm;

	@Created
	void created(Request<?> request) throws FileNotFoundException {
		if (sessionUtils.hasActiveSession(request)) {
			throw new Redirect(JadaptiveApp.class);
		}
		AuthenticationState state = authenticationService.getCurrentState();
		if (!state.getCurrentPage().equals(this.getClass())) {
			throw new Redirect(state.getCurrentPage());
		}
		if(state.hasUser())
			loginForm.setUsername(state.getUser().getUsername());
	}

	@Out(methods = HTTPMethod.GET)
	Document get(@In Document content) {

		authenticationService.decorateAuthenticationPage(content);
		return content;
	}
	
	@Bound
	boolean isUsernameReadOnly() {
		return authenticationService.getCurrentState().hasUser();
	}

	@Bound
	void loginForm() {

		try {

			if (!Boolean.getBoolean("jadaptive.webUI")) {
				throw new AccessDeniedException(
						"Web UI is currently disabled. Login to manage your account via the SSH CLI");
			}

			AuthenticationState state = authenticationService.getCurrentState();
			
			User user = userService.getUser(loginForm.getUsername());

			if (userService.verifyPassword(user, loginForm.getPassword().toCharArray())) {

				if (user instanceof PasswordEnabledUser) {

					if (((PasswordEnabledUser) user).getPasswordChangeRequired()) {
						state.getAuthenticationPages().add(ChangePassword.class);
					}
				}

				state.setAttribute(AuthenticationService.PASSWORD, loginForm.getPassword());
				throw new Redirect(authenticationService.completeAuthentication(user));
			}

			authenticationService.reportAuthenticationFailure(loginForm.getUsername());

		} catch (AccessDeniedException e) {
			Response.get().statusCode(HttpServletResponse.SC_FORBIDDEN);
			loginForm.getFeedback().error(e.getMessage());
		} catch (ObjectNotFoundException e) {

		}

		Response.get().statusCode(HttpServletResponse.SC_FORBIDDEN);
		loginForm.getFeedback().error("Bad username or password");
	}


	public interface LoginForm {

		@FormField(type = "text")
		@InputRestriction(required = true)
		String getUsername();

		@FormField(type = "password")
		@InputRestriction(required = true)
		String getPassword();

		Feedback getFeedback();
		
		void setUsername(String username);
	}
}
