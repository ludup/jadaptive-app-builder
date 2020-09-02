package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Created;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.Request;
import com.codesmith.webbits.View;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPage;

@Page
@View(contentType = "text/html", paths = { "login-reset" })
public class LoginReset extends AbstractPage {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private SessionUtils sessionUtils;

	@Created
	void created(Request<?> request) throws FileNotFoundException {
		if (sessionUtils.hasActiveSession(request)) {
			throw new Redirect(JadaptiveApp.class);
		}
		Class<?> redirect = authenticationService.resetAuthentication();
		throw new Redirect(redirect);
	}
}
