package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.AuthenticationPolicy;
import com.jadaptive.api.auth.AuthenticationPolicyService;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.UserLoginAuthenticationPolicy;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.user.User;

public abstract class AuthenticatedPage extends HtmlPage {

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private AuthenticationPolicyService policyService; 
	
	@Override
	public void onCreate() throws FileNotFoundException {
		super.onCreate();
	}

	public static void redirectIfLoginNeeded(PageCache pageCache, 
			AuthenticationService authenticationService, 
			AuthenticationPolicy policy,
			HttpServletRequest request) throws FileNotFoundException {
		if(Session.getOr(request).isEmpty()) {
			var reqUrl = request.getRequestURL();
			var query = request.getQueryString();
			if(query != null) {
				reqUrl.append('?');
				reqUrl.append(query);
			}
			authenticationService.createAuthenticationState(policy, new UriRedirect(reqUrl.toString()));
			throw new PageRedirect(pageCache.resolveDefault());
		}
	}

	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		super.beforeProcess(uri, request, response);
		
		redirectIfLoginNeeded(pageCache, authenticationService, 
				policyService.getDefaultPolicy(UserLoginAuthenticationPolicy.class),
				request);
		
		var session = Session.get(request);
		
		if((session == null || !session.getTenant().isSystem()) && isSystem()) {
			throw new FileNotFoundException();
		}
	}

	@Override
	public final void generateContent(Document document) throws IOException {
		generateAuthenticatedContent(document);			
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
