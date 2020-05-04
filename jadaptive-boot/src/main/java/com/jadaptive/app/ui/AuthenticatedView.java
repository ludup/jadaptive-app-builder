package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Created;
import com.codesmith.webbits.Redirect;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;

public abstract class AuthenticatedView {

	@Autowired
	private SessionUtils sessionUtils;
	
	@Created
	void created() throws FileNotFoundException {

		verifySession();
		onCreated();
	}

	void verifySession() {
		if(!sessionUtils.hasActiveSession(Request.get())) {
			throw new Redirect(Login.class);
		}
	}
	
	protected abstract void onCreated() throws FileNotFoundException;
}
