package com.jadaptive.app.auth;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.auth.PostAuthenticatorPage;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Extension
public class SetupPostAuthenticationPage extends HtmlPage implements PostAuthenticatorPage {

	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private PageCache pageCache;
	
	@Override
	public String getUri() {
		return "setup-post-authentication";
	}

	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		
		AuthenticationState state = authenticationService.getCurrentState();
		if(!state.hasSetupPostAuthentication()) {
			authenticationService.setupPostAuthentication(authenticationService.getCurrentState());
			throw authenticationService.completeAuthentication(
					authenticationService.getCurrentState(), 
					Optional.empty()).maybeAttachToSession(request, sessionUtils.getTimeout());
		
		} else {
			throw new PageRedirect(pageCache.resolvePage(authenticationService.getCurrentPage()));
		}
	}



	@Override
	public boolean requiresProcessing(AuthenticationState state) {
		return true;
	}

	

}
