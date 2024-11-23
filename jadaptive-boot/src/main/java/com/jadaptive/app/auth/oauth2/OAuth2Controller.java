package com.jadaptive.app.auth.oauth2;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.oauth2.OAuth2ApplicationService;
import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedContext;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Extension
@Controller
public class OAuth2Controller extends AuthenticatedController {

	static Logger LOG = LoggerFactory.getLogger(OAuth2Controller.class);

	@Autowired
	private OAuth2Service oauth2;
	
	@Autowired
	private OAuth2ApplicationService oAuth2ApplicationResourceService;
	
	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private SingletonObjectDatabase<OAuth2Configuration> config;
	
	@Autowired
	private PageCache pageCache;

	/**
	 * This is the entry point for OAuth2 clients. They call this endpoint and will
	 * be redirect to the appropriate login flow and/or approval.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="oauth2-start", method = RequestMethod.GET, produces = "text/plain")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	@AuthenticatedContext(system = true)
	public void oauth2(HttpServletRequest request, HttpServletResponse response) throws IOException {

		var oauthRequest = new OAuth2Request.Builder().forRequest(request).build();
		
		/* Check if coming from same host and different port. This 
		 * will not work 100% correctly, as session cookies are shared
		 * across different ports on the same host.
		 */
		if(oauthRequest.redirectUri() != null && request.getServerName() != null) {
			try {
				var u = new URL(oauthRequest.redirectUri());
				if(u.getHost().equals(request.getServerName())) {
					LOG.warn("**********************************************************");
					LOG.warn("                   HELLO DEVELOPER                        ");
					LOG.warn("**********************************************************");
					LOG.warn("");
					LOG.warn("It is not recommended that both sides of an OAuth transaction");
					LOG.warn("use the same hostname. It has been detected that the redirect_uri");
					LOG.warn("is using the same host as this server (probably on a different port). ");
					LOG.warn("");
					LOG.warn("This may cause issues with session cookies as those are shared");
					LOG.warn("across port nubers. As a work around, add a different hostname");
					LOG.warn("pointing to same address to your /etc/host file (or OS equivalent)");
					LOG.warn("");
					LOG.warn("**********************************************************");
				}
			}
			catch(Exception e) {
			}
		}

		String[] scope = oauthRequest.requestedScopes();
		try {
			if(scope != null)
				oauth2.getScopes(scope);
		}
		catch(IllegalArgumentException iae) {
			if(oauthRequest.redirectUri() == null)
				throw new IOException("Authorization failed.", iae);
			response.sendRedirect(errorRedirect("invalid_scope", String.format("Invalid scope in %s.", String.join(",", scope)), oauthRequest.redirectUri(), oauthRequest.state()));
			return;
		}

		var requireApplicationRegistration =  config.getObject(OAuth2Configuration.class).isRequireApplicationRegistration();
		
		/* Must have a client_id */
		if(requireApplicationRegistration && isBlank(oauthRequest.clientId())) {
			if(oauthRequest.redirectUri() == null)
				throw new IOException("Application must be provided using a client_id.");
			response.sendRedirect(errorRedirect("invalid_request", oauthRequest.redirectUri(), "Missing client_id", oauthRequest.state()));
			return;
		}
			
		
		if(isNotBlank(oauthRequest.clientId())) {
			/* Check there is an application with this client_id if it was provided or required */
			try {
				oAuth2ApplicationResourceService.find(oauthRequest.clientId());
			}
			catch(ObjectNotFoundException | AccessDeniedException rnfe) {
				if(oauthRequest.redirectUri() == null)
					throw new IOException("Unknown application.", rnfe);
				String uri = errorRedirect("unauthorized_client", "This client is not authorized.", oauthRequest.redirectUri(), oauthRequest.state());
				response.sendRedirect(uri);
				return;
			}
		}
		
		var state = authenticationService.createAuthenticationState();

		/* TODO logout if not logged in at end of OAuth */
		if(isNotBlank(oauthRequest.redirectUri())) {
			state.setHomePage(oauthRequest.redirectUri());
		}
		
		OAuth2Request.set(request.getSession(), oauthRequest);

		throw new PageRedirect(pageCache.getPage(OAuth2Approve.class));
	}

	private String errorRedirect(String error, String message, String redirectUri, String state) throws UnsupportedEncodingException {
		return redirectUri + (redirectUri.indexOf('?') == -1 ? "?" : "&") + "error=" + error +
				"&error_description=" + URLEncoder.encode(message, "UTF-8") + 
				( state == null ? "" : "&state=" + URLEncoder.encode(state, "UTF-8"));
	}

}
