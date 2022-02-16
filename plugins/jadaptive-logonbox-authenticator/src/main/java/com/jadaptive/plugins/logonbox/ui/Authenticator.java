package com.jadaptive.plugins.logonbox.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.user.EmailEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.plugins.logonbox.LogonBoxConfiguration;
import com.jadaptive.plugins.logonbox.ui.Authenticator.EmptyForm;
import com.logonbox.authenticator.AuthenticatorClient;
import com.logonbox.authenticator.AuthenticatorResponse;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Authenticator extends AuthenticationPage<EmptyForm> {

	@Autowired
	private SingletonObjectDatabase<LogonBoxConfiguration> configDatabase;
	
	@Autowired
	private PageCache pageCache;
	
	public Authenticator() {
		super(EmptyForm.class);
	}

	public String getUri() {
		return "authenticator";
	}

	@Override
	protected void generateContent(Document doc) throws FileNotFoundException {
		super.generateContent(doc);
		
		AuthenticationState state = authenticationService.getCurrentState();
		
		if(!(state.getUser() instanceof EmailEnabledUser)) {
			if(!state.getCurrentPage().equals(getClass())) {
				throw new PageRedirect(pageCache.resolvePage(state.getCurrentPage()));
			}
			throw new AccessDeniedException("Authenticatior requires email address");
		}
		
		String email = ((EmailEnabledUser)state.getUser()).getEmail();
		String hostname = configDatabase.getObject(LogonBoxConfiguration.class).getDirectoryHostname();
		int port = configDatabase.getObject(LogonBoxConfiguration.class).getDirectoryPort();
		
		if(StringUtils.isBlank(hostname)) {
			throw new PageRedirect(pageCache.resolvePage("login"));
		}
		doc.selectFirst("#avatar").attr("src", 
				String.format("https://%s:%d/app/api/userLogo/fetch/%s/128", hostname, port, email));
	}

	@Override
	protected boolean doForm(Document document, AuthenticationState state, EmptyForm form)
			throws AccessDeniedException {
		
		if(Objects.isNull(state.getUser())) {
			throw new AccessDeniedException("Authenticator requires known user to authenticate");
		}
		
		User authenticatingUser = state.getUser();
		
		if(!(authenticatingUser instanceof EmailEnabledUser)) {
			throw new AccessDeniedException("Authenticatior requires email address");
		}
		
		EmailEnabledUser user = (EmailEnabledUser)authenticatingUser;
		
		LogonBoxConfiguration config = configDatabase.getObject(LogonBoxConfiguration.class);
		String hostname = config.getDirectoryHostname();
		int port = config.getDirectoryPort();
		
		AuthenticatorClient client = new AuthenticatorClient(hostname, port);
		
		if(config.getDebug()) {
			client.enableDebug();
		}
		
		client.setAuthorizeText(config.getAuthorizeAction());
		client.setPromptText(replaceVariables(config.getAuthorizePrompt(), 
				user, config.getApplicationName(),
				config.getDirectoryHostname()));
		client.setRemoteName(config.getApplicationName());
		

		try {
			String email = ((EmailEnabledUser)authenticatingUser).getEmail();
			AuthenticatorResponse resp = client.authenticate(email);
			
			if(resp.verify()) {
				return true;
			} else {
		    	Request.response().setStatus(HttpStatus.FORBIDDEN.value());
				document.selectFirst("#feedback").append("<div class=\"alert alert-danger\">Invalid credentials</div>");
				return false;
			}
		} catch (IOException e) {
	    	Request.response().setStatus(HttpStatus.FORBIDDEN.value());
			document.selectFirst("#feedback").append("<div class=\"alert alert-danger\">" + e.getMessage() + "</div>");
			return false;
		}
		

	}

	private String replaceVariables(String authorizeAction, EmailEnabledUser user, String appName, String hostname) {
		return authorizeAction.replace("${username}", user.getUsername())
				.replace("${email}", user.getEmail())
				.replace("${hostname}", hostname)
				.replace("${applicationName}", appName);
	}

	interface EmptyForm {
		
	}
}
