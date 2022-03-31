package com.jadaptive.plugins.logonbox.ui;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.ErrorPage;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.EmailEnabledUser;
import com.jadaptive.plugins.logonbox.LogonBoxConfiguration;
import com.jadaptive.utils.Utils;
import com.logonbox.authenticator.AuthenticatorClient;
import com.logonbox.authenticator.AuthenticatorRequest;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class AuthenticatorStart extends HtmlPage {

	static final String AUTHENTICATOR_REQUEST = "authenticatorRequest";

	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private SingletonObjectDatabase<LogonBoxConfiguration> configDatabase;
	
	@Autowired
	private PageCache pageCache;
	
	public AuthenticatorStart() {
	}

	public String getUri() {
		return "authenticator-start";
	}

	@Override
	protected void generateContent(Document doc) throws FileNotFoundException {
		
		AuthenticationState state = authenticationService.getCurrentState();
		
		if(!(state.getUser() instanceof EmailEnabledUser)) {
			if(!state.getCurrentPage().equals(getClass())) {
				throw new PageRedirect(pageCache.resolvePage(state.getCurrentPage()));
			}
			throw new AccessDeniedException("Authenticatior requires email address");
		}
		
		LogonBoxConfiguration config = configDatabase.getObject(LogonBoxConfiguration.class);
		String hostname = config.getDirectoryHostname();
		int port = config.getDirectoryPort();
		
		AuthenticatorClient client = new AuthenticatorClient(hostname, port);
		client.setAuthorizeText(config.getAuthorizeAction());
		client.setPromptText(replaceVariables(config.getAuthorizePrompt(), 
				(EmailEnabledUser)state.getUser(),
				config.getApplicationName(), 
				config.getDirectoryHostname()));
		client.setRemoteName(config.getApplicationName());
		
		if(config.getDebug()) {
			client.enableDebug();
		}
		
		String email = ((EmailEnabledUser)state.getUser()).getEmail();
		
		if(StringUtils.isBlank(hostname)) {
			throw new PageRedirect(pageCache.resolvePage("login"));
		}
		
		try {
			AuthenticatorRequest request = client.generateRequest(email,
				Utils.getBaseURL(Request.get().getRequestURL().toString())
					+ "/app/ui/authenticator-finish/{response}");
	
			Request.get().getSession().setAttribute(AUTHENTICATOR_REQUEST, request);
			
			throw new UriRedirect(request.getUrl());
		
		} catch(Throwable e) {
			if(e instanceof UriRedirect) {
				throw (UriRedirect) e;
			}
			throw new PageRedirect(new ErrorPage(e));
		}
	}
	
	private String replaceVariables(String prompt, EmailEnabledUser user, String appName, String hostname) {
		return prompt.replace("{username}", user.getUsername())
				.replace("{email}", user.getEmail())
				.replace("{hostname}", hostname)
				.replace("{applicationName}", appName);
	}
}
