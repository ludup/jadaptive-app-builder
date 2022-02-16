package com.jadaptive.plugins.logonbox.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.ErrorPage;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.RequestPage;
import com.logonbox.authenticator.AuthenticatorRequest;
import com.logonbox.authenticator.AuthenticatorResponse;

@Extension
@RequestPage(path = "authenticator-finish/{response}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class AuthenticatorFinish extends HtmlPage {

	@Autowired
	private AuthenticationService authenticationService; 

	@Autowired
	private PageCache pageCache;
	
	String response;
	
	public AuthenticatorFinish() {
	}

	public String getUri() {
		return "authenticator-finish";
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	@Override
	protected void generateContent(Document doc) throws FileNotFoundException {
		
		
		AuthenticatorRequest request = (AuthenticatorRequest) 
				Request.get().getSession().getAttribute(AuthenticatorStart.AUTHENTICATOR_REQUEST);

		if(Objects.isNull(request)) {
			throw new PageRedirect(new ErrorPage(
					new IOException("Missing payload or authenticator!"), 
						pageCache.resolvePage("login")));
		}
		
		AuthenticationState state = authenticationService.getCurrentState();
		
		try {
			AuthenticatorResponse resp = request.processResponse(response);
			
			if(resp.verify()) {
				throw new PageRedirect(pageCache.resolvePage(authenticationService.completeAuthentication(state)));
			}
			
			authenticationService.reportAuthenticationFailure(state);
		} catch (IOException e) {
			authenticationService.reportAuthenticationFailure(state);
			throw new PageRedirect(new ErrorPage(e, pageCache.resolvePage("login")));
		}
		
		

	}
}
