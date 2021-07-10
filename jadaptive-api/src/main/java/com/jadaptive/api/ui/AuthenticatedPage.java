package com.jadaptive.api.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.user.User;

public abstract class AuthenticatedPage extends HtmlPage {

	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private PageCache pageCache;
	
	ThreadLocal<Session> currentSession = new ThreadLocal<>();
	
	@Override
	public final void generateContent(Document document) throws FileNotFoundException {
		
		if(!sessionUtils.hasActiveSession(Request.get())) {
			AuthenticationState state = authenticationService.getCurrentState();
			state.setHomePage(Request.get().getRequestURI());
			throw new PageRedirect(pageCache.resolvePage("login"));
		}
		
		try {
			currentSession.set(sessionUtils.getActiveSession(Request.get()));
			generateAuthenticatedContent(document);
			
		} finally {
			currentSession.remove();
		}
	}

	@Override
	protected void documentComplete(Document document) {
		PageHelper.appendScript(document, "/app/content/jadaptive-session.js");
		super.documentComplete(document);
	}


	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException {
		
	}
	
	protected User getCurrentUser() {
		return getCurrentSession().getUser();
	}
	
	protected Session getCurrentSession() {
		return currentSession.get();
	}
}
