package com.jadaptive.plugins.universal;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.Form;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AbstractPage;
import com.jadaptive.api.user.EmailEnabledUser;
import com.sshtools.universal.UniversalAuthenticatorClient;

@Page
@View(contentType = "text/html", paths = { "ua"})
@ClasspathResource
public class UniversalAuthentication extends AbstractPage {

static Logger log = LoggerFactory.getLogger(UniversalAuthentication.class);
	
	public static final String AUTHENTICATOR_STATE = "authenticatorState";
	
	
	@Autowired
	private AuthenticationService authenticationService; 

	@Autowired
	private UAService uaService;
	
	@Out(methods = HTTPMethod.GET)
	Document get(@In Document content) {
		
		AuthenticationState state = authenticationService.getCurrentState();
    	if(Objects.isNull(state.getUser())) {
    		throw new Redirect(authenticationService.resetAuthentication());
    	}
		
    	authenticationService.decorateAuthenticationPage(content);
		
    	if(uaService.hasCredentials(state.getUser())) {
    		showAuthorization(content, state);
    	} else {
    		showRegistration(content, state);
    	}
    	
    	
		return content;
	}
	
	private void showAuthorization(Document content, AuthenticationState state) {
		
		Element el = content.selectFirst("#content");
		el.append("<p><i id=\"autoSubmit\" class=\"fad fa-mobile-android fa-3x\"></i></p>");
		el.append("<p>An authorization request has been sent to your mobile phone. Please authorize your login to continue.</p>");
		el.append("<script type=\"text/javascript\">$(document).ready(function() { $('#loginForm'}).submit(); });</script>");
		
		state.setAttribute(AUTHENTICATOR_STATE, UniversalAuthenticationState.REGISTERED);
	}

	private void showRegistration(Document content, AuthenticationState state) {
		
		if(!(state.getUser() instanceof EmailEnabledUser)) {
			throw new IllegalStateException("UA authentication requires an email enabled user");
		}
		
		EmailEnabledUser u = (EmailEnabledUser) state.getUser();
		
		if(StringUtils.isBlank(u.getEmail())) {
			throw new IllegalStateException("User requires an email address to register for UA authentication");
		}
		
		StringBuffer buffer = new StringBuffer();
		
		boolean allowSkip = true;
		String username = u.getUsername();
		String email = u.getEmail();
		String serverHost = Request.get().getServerName();
		int serverPort = getServerPort();
		String serverPath = "/ua-register";
		String keyServerHost = "gateway.jadaptive.com";
		int keyServerPort = 443;
		
		buffer.append("https://gateway.jadaptive.com/app/api/agent/qr?username=");
		buffer.append(username);
		buffer.append("&email=");
		buffer.append(email);
		buffer.append("&serverHost=");
		buffer.append(serverHost);
		buffer.append("&serverPort=");
		buffer.append(serverPort);
		buffer.append("&serverPath=");
		buffer.append(serverPath);
		buffer.append("&keyServerHost=");
		buffer.append(keyServerHost);
		buffer.append("&keyServerPort=");
		buffer.append(keyServerPort);
		
		Element el = content.selectFirst("#content");
		el.append("<h3>Setup Universal Authenticator</h3>");
		el.append("<p>In future you will be required to authenticate with the Universal Authenticator app. You can download the app from the appropriate app store.</p>");
		el.append("<p><a href=\"https://play.google.com/store/apps/details?id=com.sshtools.mobile.agent\">\n" + 
				"			    <img src=\"/app/ui/com/jadaptive/plugins/universal/playstore.png\" height=\"50\"></a>\n" + 
				"			    \n" + 
				"			<a href=\"https://play.google.com/store/apps/details?id=com.sshtools.mobile.agent\">\n" + 
				"			  <img src=\"/app/ui/com/jadaptive/plugins/universal/appstore.png\" height=\"50\"></a></p>");
		el.append("<p>Open the app and when prompted, click on Scan QR button and scan the QR below.</p>");
		el.append("When your app is registered, click on the Next button below to proceed.</p>");
		el.append("<p><img src=\"" + buffer.toString() + "\"></p>");
		if(allowSkip) {
			el.append("<p><input type=\"checkbox\" name=\"skip\" value=\"true\">&nbsp;<span>I would rather setup this up at another time.</span></p>");
		}
		
		state.setAttribute(AUTHENTICATOR_STATE, UniversalAuthenticationState.UNREGISTERED);
	}
	 
    private int getServerPort() {
		
    	String header = Request.get().getHeader("X-Forwarded-Port");
    	if(Objects.nonNull(header)) {
    		return Integer.parseInt(header);
    	}
    	return Request.get().getServerPort();
	}

	@Out(methods = HTTPMethod.POST)
    Document post(@In Document content, @Form RegistrationForm form) {

    	AuthenticationState state = authenticationService.getCurrentState();
    	if(Objects.isNull(state.getUser())) {
    		throw new IllegalStateException("Icon password must be used after establishing the user");
    	}
    	
    	if(Objects.nonNull(form.getSkip()) && form.getSkip()) {
    		throw new Redirect(authenticationService.completeAuthentication(state.getUser()));
    	}
    	
    	/**
    	 * Wait for authentication
    	 */
    	
    	UniversalAuthenticatorClient uac = new UniversalAuthenticatorClient(uaService.getCredentials(state.getUser()));
    	try {
			uac.authenticate("Authorize your login to " + Request.get().getServerName());
			throw new Redirect(authenticationService.completeAuthentication(state.getUser()));
		} catch (IOException e) {
			authenticationService.reportAuthenticationFailure(state.getUser().getUsername());
			/**
			 * TODO feedback
			 */
		}
    	
    	
    	authenticationService.reportAuthenticationFailure(state.getUser().getUsername());
   
    	return content;

    }

    interface RegistrationForm {
    	Boolean getSkip();
    }
}
