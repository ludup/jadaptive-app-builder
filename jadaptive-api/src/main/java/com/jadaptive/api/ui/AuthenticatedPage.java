package com.jadaptive.api.ui;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Created;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.Request;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.user.User;

public abstract class AuthenticatedPage extends AbstractPage {

	@Autowired
	private SessionUtils sessionUtils;

	@Autowired
	private AuthenticationService authenticationService;

	@Created
	void created() throws FileNotFoundException {

		verifySession();
		onCreated();
	}

	void verifySession() {
		if (!sessionUtils.hasActiveSession(Request.get())) {
			throw new Redirect(authenticationService.resetAuthentication());
		}
	}

	protected User getCurrentUser() {
		return sessionUtils.getCurrentUser();
	}

	protected void onCreated() throws FileNotFoundException {
	};
}
