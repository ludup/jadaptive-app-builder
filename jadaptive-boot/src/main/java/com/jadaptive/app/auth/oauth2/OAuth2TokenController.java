package com.jadaptive.app.auth.oauth2;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
import com.jadaptive.api.auth.oauth2.GrantType;
import com.jadaptive.api.auth.oauth2.OAuth2Application;
import com.jadaptive.api.auth.oauth2.OAuth2ApplicationService;
import com.jadaptive.api.auth.oauth2.OAuth2AuthorizationService;
import com.jadaptive.api.auth.oauth2.OAuth2ErrorResponse;
import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.auth.oauth2.OAuth2Response;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.auth.oauth2.OAuth2Token;
import com.jadaptive.api.auth.oauth2.OAuth2TokenService;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedContext;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Extension
@Controller
public class OAuth2TokenController extends AuthenticatedController {
	final static Logger LOG = LoggerFactory.getLogger(OAuth2TokenController.class);

	@Autowired
	private OAuth2ApplicationService resourceService;
	@Autowired
	private OAuth2TokenService tokenService;
	@Autowired
	private OAuth2Service oauth;
	@Autowired
	private PermissionService permissionService; 
	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private SingletonObjectDatabase<OAuth2Configuration> config;

	@RequestMapping(value = "pair", method = RequestMethod.GET)
	public void pair(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if(request.getQueryString() == null)
			response.sendRedirect("/app/ui/oauth2-device");
		else
			response.sendRedirect("/app/ui/oauth2-device?" + request.getQueryString());
	}

	@RequestMapping(value = "oauth2/token", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext(system = true)
	public OAuth2Response token(HttpServletRequest request, HttpServletResponse response) throws Exception {

		try {

			OAuth2Application application = null;
			
			var oauth2Config = config.getObject(OAuth2Configuration.class);
			var requireApplicationRegistration =  oauth2Config.isRequireApplicationRegistration();
			var clientId = request.getParameter("client_id");
			if (clientId == null && requireApplicationRegistration)
				throw new IllegalArgumentException("This type of authorize is unsupported.");

			if(clientId != null) {
				try {
					application = resourceService.getObjectByUUID(clientId);
				}
				catch(ObjectNotFoundException nfe) {
					application = resourceService.byName(clientId);
				}
			}
			
			var grantTypeStr = request.getParameter("grant_type");
			if (isNotBlank(grantTypeStr)) {
				/* Normal authorization code type */
				var grantType = GrantType.fromGrantType(grantTypeStr);
				if (grantType.equals(GrantType.AUTHORIZATION_CODE)) {

					var redirectUri = request.getParameter("redirect_uri");
					if (isBlank(redirectUri))
						throw new IllegalArgumentException("Redirect URI must be supplied.");

					if (application != null && application.getRedirectUris().size() > 0) {
						LOG.info("Checking {0} redirect URIs to see if {} matches", application.getRedirectUris().size(), redirectUri);
						var matched = false;
						for(var applicationRedirectUri : application.getRedirectUris()) {
							if(redirectUri.matches(applicationRedirectUri)) {
								matched = true;
								LOG.info("{} DOES match {}", redirectUri, applicationRedirectUri);
								break;
							}
							else
								LOG.info("{} does NOT match {}", redirectUri, applicationRedirectUri);
						}
						if(!matched) {
							throw new UnauthorizedException(String.format("The redirect URL %s is not allowed.", redirectUri));
						}
					}
					
					/* Turn an authorization code into a token */
					var code = request.getParameter("code");
					if (isBlank(code))
						throw new IllegalArgumentException("Authorization code is required.");
					var req = oauth.popAuthCodeRequest(code);
					
					var codeVerifier = request.getParameter("code_verifier");
					if(isBlank(codeVerifier)) {
						/* Need a secret if not using a code verifier */
						if(application == null) {
							throw new IllegalArgumentException("Client identifier must be supplied.");
						}
						else if(isNotBlank(application.getSecret())) {
							var secret = request.getParameter("client_secret");
							if (isBlank(secret))
								throw new IllegalArgumentException("Client secret must be supplied.");
							checkAppSecret(application, secret);
						}
					}
					else {
						var challlenge = req.getCodeChallenge();
						if(isBlank(challlenge))
							throw new IllegalStateException("Expected code challenge in authorization request.");
						 
						if("s256".equalsIgnoreCase(req.getCodeChallengeMethod())) {
							/* Hash and base64 encoded */
							var digest = MessageDigest.getInstance("SHA256");
							digest.reset();
							digest.update(codeVerifier.getBytes("UTF-8"));
							var hashed = digest.digest();
							var encoded = Base64.getEncoder().encodeToString(hashed);
							if(!encoded.equals(challlenge)) {
								throw new UnauthorizedException();
							}
						}
						else {
							/* Plain verifier */
							if(!codeVerifier.equals(challlenge)) {
								throw new UnauthorizedException();
							}
						} 
					}

					return createToken(application, req.getPrincipal(), req.getNonce(), req.getScopeNames(), oauth2Config);
					
				}
				else if (grantType.equals(GrantType.PASSWORD)) {
					/* Can be used to exchange a username and password for an access token directly */
					
					var username = request.getParameter("username");
					var password = request.getParameter("password");
					if (isBlank(username))
						throw new IllegalArgumentException("Username is required.");
					if (!isBlank(password))
						throw new IllegalArgumentException("Password is required.");
					
					/* This session exist just for the duration of getting the token */
					try {
						try(var result  = authenticationService.logonUser(username, password, getCurrentTenant(), request.getRemoteAddr(), request.getHeader("User-Agent"))) {
							return createToken(application, result.session().get().getUser(), null, request.getParameter("scope") == null ? new String[0] : request.getParameterValues("scope"), oauth2Config);
						}
					}
					catch(Exception e) {
						LOG.error("Failed to create token.", e);
						response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
						return null;
					}
				}
				else if (grantType.equals(GrantType.CLIENT_CREDENTIALS)) {
					/* Applications may need an access token to act on behalf of themselves rather than a user */
					var secret = request.getParameter("client_secret");
					if (isBlank(secret))
						throw new IllegalArgumentException("Client secret is required.");
					checkAppSecret(application, secret);
					return createToken(application, null, null, request.getParameter("scope") == null ? new String[0] : request.getParameterValues("scope"), oauth2Config);
				}
				else if (grantType.equals(GrantType.REFRESH_TOKEN)) {
					/* Refresh an expired token */
					if(application != null) {
						var secret = request.getParameter("client_secret");
						if (isBlank(secret))
							throw new IllegalArgumentException("Client secret is required.");
						checkAppSecret(application, secret);
					}

					var refreshToken = request.getParameter("refresh_token");
					if (isBlank(refreshToken))
						throw new IllegalArgumentException("Refresh token is required.");
					
					var oldToken = tokenService.byRefreshToken(refreshToken);
					if(!oldToken.getRefreshToken().equals(refreshToken))
						throw new UnauthorizedException();
					
					/* Different scope may be requested, but no new scopes may be requested */
					var scope = oldToken.getScopes();
					var newScope = request.getParameterValues("scope");
					if(newScope != null) {
						for(var s : newScope) {
							if(!scope.contains(s))
								throw new IllegalArgumentException("Cannot request additional new scope.");
						}
						scope = Arrays.asList(newScope);
					}

					try(var ctx = permissionService.userContext(oldToken.getOwner())){
						var newTkn = createToken(application, oldToken.getOwner(), oldToken.getNonce(), scope.toArray(new String[0]), oauth2Config);
						tokenService.deleteObject(oldToken);
						return newTkn;
					}
				}
				else if (grantType.equals(GrantType.DEVICE)) {
					var deviceCode = request.getParameter("device_code");
					
					var pending = oauth.getPendingDeviceByDeviceCode(deviceCode);
					if(pending != null) {
						switch(pending.status()) {
						case PENDING:
							return new OAuth2ErrorResponse("authorization_pending");
						case REJECTED:
							try {
								return new OAuth2ErrorResponse("access_denied");
							}
							finally {
								oauth.removeDevice(deviceCode);
							}
						case APPROVED:
							try {
								return createToken(application, pending.user(), null, pending.request().requestedScopes(), oauth2Config);
							}
							finally {
								oauth.removeDevice(deviceCode);
							}
						}
					}
					else {
						LOG.warn("Request for expired pending device code {}", deviceCode);
						return new OAuth2ErrorResponse("expired_token");
					}
				}
			}

			throw new IllegalArgumentException("This type of grant is unsupported.");
		} catch(IllegalArgumentException iae) {
			LOG.error("Invalid request.", iae);
			return new OAuth2ErrorResponse("invalid_request", iae);
		} catch(UnauthorizedException ue) {
			LOG.error("Unauthorized.", ue);
			return new OAuth2ErrorResponse("access_denied", ue);
		} catch(Exception e) {
			LOG.error("Token exchange failed.", e);
			return new OAuth2ErrorResponse("server_error", e);
		}
	}
	
	@RequestMapping(value = "oauth2/device", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext(system = true)
	public OAuth2Response device(HttpServletRequest request, HttpServletResponse response) throws Exception {

		try {
			/* https://datatracker.ietf.org/doc/html/rfc8628#section-3.4 */
			OAuth2Application application = null;
			
			var oauth2Config = config.getObject(OAuth2Configuration.class);
			var requireApplicationRegistration =  oauth2Config.isRequireApplicationRegistration();
			var clientId = request.getParameter("client_id");
			if (clientId == null && requireApplicationRegistration)
				throw new IllegalArgumentException("This type of authorize is unsupported.");

			var scopes = request.getParameterValues("scope");
			if(scopes == null)
				scopes = new String[0];

			if(clientId != null) {
				try {
					application = resourceService.getObjectByUUID(clientId);
				}
				catch(ObjectNotFoundException nfe) {
					application = resourceService.byName(clientId);
				}
			}
			
			if(application != null && isNotBlank(application.getSecret())) {
				var secret = request.getParameter("client_secret");
				if (isBlank(secret))
					throw new IllegalArgumentException("Client secret must be supplied.");
				checkAppSecret(application, secret);
			}
			
			var uri = OAuth2AuthorizationService.resolveUri(request, "/pair");
			
			var oauth2Request = new OAuth2Request.Builder().
					forRequest(request).
					withResponseType("device_code").
					build();
			
			/* Give all the scopes involved the chance to modify the
			 * URL. They can use this to send the client via another endpoint
			 * to add additional context, and then redirect on to the original
			 * verification_uri. 
			 */
			for(var scope : oauth.getScopes(scopes)) {
				uri = scope.decorateVerificationUri(oauth2Request, uri);
			}
			
			
			return oauth.requestDevice(oauth2Request, uri);
			
		} catch(IllegalArgumentException iae) {
			LOG.error("Invalid request.", iae);
			return new OAuth2ErrorResponse("invalid_request", iae);
		} catch(UnauthorizedException ue) {
			LOG.error("Unauthorized.", ue);
			return new OAuth2ErrorResponse("access_denied", ue);
		} catch(Exception e) {
			LOG.error("Token exchange failed.", e);
			return new OAuth2ErrorResponse("server_error", e);
		}
	}
	
	protected void checkAppSecret(OAuth2Application application, String secret) throws IOException, UnauthorizedException {
		var appSecret = StringUtils.isBlank(application.getSecret()) ? "" : application.getSecret();
		if(isBlank(appSecret) || !secret.equals(appSecret))
			throw new UnauthorizedException();
	}

	protected OAuth2Response createToken(OAuth2Application application, User user, String nonce, String[] scope, OAuth2Configuration oauth2Config)
			throws AccessDeniedException {
		var currentTenant = getCurrentTenant();
		
		/* Double check the user still has permission for the requested scopes. If this is a refresh token, this may have
		 * changed since the token was issued
		 */
		for(OAuth2Scope s : oauth.getScopes(scope)) {
			s.verifyPermissions(Optional.empty(), user);
		}
		
		var token = new OAuth2Token();
		token.setName(OAuth2AuthorizationService.genToken());
		if((application != null && application.isIssueRefreshToken()) || oauth2Config.isIssueRefreshTokenForUnregisteredRequests()) {
			token.setRefreshToken(OAuth2AuthorizationService.genToken());
		}
		token.setOwner(user);
		token.setTenant(currentTenant.getUuid());
		token.setScopes(Arrays.asList(scope));
		token.setNonce(nonce);

		tokenService.saveOrUpdate(token);
		
		if(application == null) {
			var expTime = oauth2Config.getDefaultExpiryTime();
			token.setExpires(System.currentTimeMillis() + (expTime * 1000));
			tokenService.saveOrUpdate(token);		
			return new BearerToken(token, expTime);
		}
		else {
			token.setApplication(application);
			token.setExpires(System.currentTimeMillis() + (application.getExpiryTime() * 1000));
			tokenService.saveOrUpdate(token);				
			return new BearerToken(token, application.getExpiryTime());
		}
	}
}
