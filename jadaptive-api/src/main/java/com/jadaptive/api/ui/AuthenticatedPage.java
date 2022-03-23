package com.jadaptive.api.ui;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.user.User;

public abstract class AuthenticatedPage extends HtmlPage {

	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private PageCache pageCache;
	
	ThreadLocal<User> currentUser = new ThreadLocal<>();
	
	
	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		super.beforeProcess(uri, request, response);
		
		if(!sessionUtils.hasActiveSession(request)) {
			AuthenticationState state = authenticationService.getCurrentState();
			state.setHomePage(Request.get().getRequestURI());
			throw new PageRedirect(pageCache.resolvePage("login"));
		}
		
		currentUser.set(sessionUtils.getActiveSession(Request.get()).getUser());
	}

	@Override
	public final void generateContent(Document document) throws FileNotFoundException {
		
		try {
			generateAuthenticatedContent(document);			
		} finally {
			currentUser.remove();
		}
	}

	@Override
	protected void documentComplete(Document document) {
		PageHelper.appendScript(document, "/app/content/jadaptive-session.js");
		super.documentComplete(document);
	}


	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException {
		
	}
	
	protected void setCurrentUser(User user) {
		currentUser.set(user);
	}
	
	protected User getCurrentUser() {
		return currentUser.get();
	}

}
