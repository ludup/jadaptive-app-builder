package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	ThreadLocal<Session> currentSession = new ThreadLocal<>();
	
	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		super.beforeProcess(uri, request, response);
		
		if(!sessionUtils.hasActiveSession(request)) {
			throw new PageRedirect(pageCache.resolveDefault());
		}
		
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
