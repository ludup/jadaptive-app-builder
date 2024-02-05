package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.user.User;

public abstract class AuthenticatedPage extends HtmlPage {

	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	ThreadLocal<Session> currentSession = new ThreadLocal<>();

	@Override
	public void onCreate() throws FileNotFoundException {
		super.onCreate();
		
		var request = Request.get();
		if(!sessionUtils.hasActiveSession(request)) {
			var reqUrl = request.getRequestURL();
			var query = request.getQueryString();
			if(query != null) {
				reqUrl.append('?');
				reqUrl.append(query);
			}
			authenticationService.createAuthenticationState(new UriRedirect(reqUrl.toString()));
			throw new PageRedirect(pageCache.resolveDefault());
		}
	}

	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		super.beforeProcess(uri, request, response);
		
		currentSession.set(sessionUtils.getActiveSession(Request.get()));
		
		if(!currentSession.get().getTenant().isSystem() && isSystem()) {
			throw new FileNotFoundException();
		}
	}

	@Override
	public final void generateContent(Document document) throws IOException {
		
		try {
			generateAuthenticatedContent(document);			
		} finally {
			currentSession.remove();
		}
	}

	@Override
	protected void documentComplete(Document document) throws IOException {
		PageHelper.appendHeadScript(document, "/app/content/jadaptive-session.js");
		super.documentComplete(document);
	}


	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		
	}
	
	protected User getCurrentUser() {
		return permissionService.getCurrentUser();
	}
	
	protected boolean isSystem() {
		return false;
	}
}
