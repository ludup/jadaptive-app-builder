package com.jadaptive.app.saml.idp.filters;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionUtils;

@Component
public class CustomLogoutFilter implements LogoutHandler {

	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private SessionUtils sessionUtils;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
	
		Session session = sessionUtils.getSession(Request.get());
		if(Objects.nonNull(session)) {
			sessionService.closeSession(session);
		}
	}

}
