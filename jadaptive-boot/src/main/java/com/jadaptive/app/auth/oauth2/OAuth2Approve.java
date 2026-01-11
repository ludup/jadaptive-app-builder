package com.jadaptive.app.auth.oauth2;


import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.oauth2.GrantType;
import com.jadaptive.api.auth.oauth2.OAuth2ApplicationService;
import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.auth.oauth2.Strictness;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Extension
@ModalPage
@RequestPage(path = "oauth2-approve")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils" })
@PageProcessors(extensions = { "i18n" })
public class OAuth2Approve extends AuthenticatedPage
		implements FormProcessor<OAuth2Approve.ApproveForm> {
	final static Logger LOG = LoggerFactory.getLogger(OAuth2Approve.class);

	public interface ApproveForm {
		boolean isApproved();
	}
	
	@Autowired
	private OAuth2Service oauth;

	@Autowired
	private I18nService i18nService;

	@Autowired
	private PageCache pageCache;

	@Autowired
	private OAuth2ApplicationService oAuth2ApplicationService;

	@Autowired
	private SingletonObjectDatabase<OAuth2Configuration> config;

	@Override
	public String getUri() {
		return "oauth2-approve";
	}

	@Override
	public Class<ApproveForm> getFormClass() {
		return ApproveForm.class;
	}

	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		if(OAuth2Request.get(Request.get().getSession()).isEmpty()) {
			throw new PageRedirect(pageCache.getHomePage());
		}

		var oauthRequest = OAuth2Request.get(request.getSession()).orElseThrow(() -> new IllegalStateException("Not OAuth2 request in authentication state."));
		var scopes = oauth.getScopes(oauthRequest.requestedScopes());
		var strictness = Strictness.forScopes(scopes);
		if(strictness == Strictness.LAX && "device_code".equals(oauthRequest.requestedGrantType())) {
			try {
				User cuser = getCurrentUser();
				approve(oauthRequest, uri, cuser, scopes);
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}
		else
			super.beforeProcess(uri, request, response);
	}

	public boolean processForm(Document document, ApproveForm form) throws Exception {
		var oauthRequest = OAuth2Request.get(Request.get().getSession()).orElseThrow(() -> new IllegalStateException("Not OAuth2 request in authentication state."));
		var redirectUri = oauthRequest.redirectUri();
		var user = getCurrentUser();
		var scopes = oauth.getScopes(oauthRequest.requestedScopes());
		
		if (form.isApproved()) {

			LOG.info("Approving OAuth request {}", oauthRequest);
			
			return approve(oauthRequest, redirectUri, user, scopes);
		} else {
			LOG.info("Rejecting OAuth request {}", oauthRequest);

			return reject(oauthRequest, redirectUri, scopes, i18nService.format("oauth2", Locale.getDefault(), "rejectedApproval.reason"));
			
		}
	}

	private boolean reject(OAuth2Request oauthRequest, String redirectUri, Set<OAuth2Scope> scopes, String reason)
			throws FileNotFoundException, UnsupportedEncodingException {
		oauth.getScopes(oauthRequest.requestedScopes()).forEach(scope -> scope.onRejected(oauthRequest));

		var responseType = oauthRequest.requestedResponseType();
		if ("device_code".equals(responseType)) {
			oauth.rejectUserCode(oauthRequest.userCode());
			if(scopes.size() == 1) {
				var firstScope = scopes.iterator().next();
				Feedback.warning(firstScope.getBundle(), "info." + firstScope.getId() + ".rejected", reason);
			}
			else
				Feedback.warning("oauth2", "info.rejectedApproval", reason, scopesToUl(scopes));
			throw new PageRedirect(pageCache.getHomePage());
		}
		else {
			if (isBlank(redirectUri))
				throw new IllegalArgumentException("Redirect URI must be supplied.");
			throw getErrorResponse(redirectUri, "access_denied", "Authorization to this resource rejected");
		}
	}

	private boolean approve(OAuth2Request oauthRequest, String redirectUri, User user, Set<OAuth2Scope> scopes)
			throws UnsupportedEncodingException {
		try {

			var clientId = oauthRequest.clientId();
			
			if (config.getObject(OAuth2Configuration.class).isRequireApplicationRegistration() && 
					isBlank(clientId))
				throw new IllegalArgumentException("Application must be provided using the client_id.");
			
			
			if (isBlank(redirectUri))
				throw new IllegalArgumentException("Redirect URI must be supplied.");
			
			if (isNotBlank(clientId) && config.getObject(OAuth2Configuration.class).isRequireApplicationRegistration()) {

				var application = oAuth2ApplicationService.find(clientId);
				
				if (application.getRedirectUris().size() > 0) {
					var matched = false;
					LOG.info("Checking {0} redirect URIs to see if {} matches", application.getRedirectUris().size(), redirectUri);
					for(var applicationRedirectUri : application.getRedirectUris()) {
						if(redirectUri.matches(applicationRedirectUri)) {
							LOG.info("{} DOES match {}", redirectUri, applicationRedirectUri);
							matched = true;
							break;
						}
						else
							LOG.info("{} does NOT match {}", redirectUri, applicationRedirectUri);
					}
					if(!matched) {
						throw new UnauthorizedException("This redirect URL is not allowed.");
					}
				}

				if(application.getGrantType() != GrantType.AUTHORIZATION_CODE) {
					throw new IllegalStateException("Application doesn't allow requests of this type.");
				}
			}
			
			/* https://aaronparecki.com/oauth-2-simplified/ */
			var responseType = oauthRequest.requestedResponseType();
			if ("code".equals(responseType)) {
				/* Requesting a one time authorized for the specified scope */
				var nonce = oauthRequest.requestedNonce();  
				var codeChallenge = oauthRequest.requestedCodeChallenge();  
				var codeChallengeMethod = oauthRequest.requestedCodeChallengeMethod();

				scopes.forEach(scope -> { 
					scope.verifyPermissions(Optional.of(oauthRequest), user);
				});
				
				var requestAuthCode = oauth.requestAuthCode(new OAuth2AuthCodeRequest(
						scopes, redirectUri, user, nonce, codeChallenge, codeChallengeMethod));
				var redirectTo = redirectUri + (redirectUri.indexOf('?') == -1 ? "?" : "&") + "code=" + requestAuthCode;
				
				if (isNotBlank(oauthRequest.state()))
					redirectTo += "&state=" + oauthRequest.state();
				
				scopes.forEach(scope -> scope.onApproved(oauthRequest));
				
				throw new UriRedirect(redirectTo);
			}
			else if ("device_code".equals(responseType)) {

				try {
					scopes.forEach(scope -> scope.verifyPermissions(Optional.of(oauthRequest), user));
				}
				catch(Exception e) {
					LOG.error("Approval failed.", e);
					Feedback.error("oauth2", "error.approvalFailed", e.getMessage());
					try {
						reject(oauthRequest, redirectUri, scopes, e.getMessage());
					}
					catch(Exception e2) {
						LOG.error("Failed to reject failed approval.", e);
					}
					
					throw new PageRedirect(pageCache.getHomePage());
				}
				
				try {
					oauth.approveUserCode(oauthRequest.userCode(), user);
					
					if(scopes.size() == 1) {
						var firstScope = scopes.iterator().next();
						Feedback.success(firstScope.getBundle(), "info." + firstScope.getId() + ".approved");
					}
					else
						Feedback.success("oauth2", "info.approvedApproval", scopesToUl(scopes));
					
					scopes.forEach(scope -> scope.onApproved(oauthRequest));
				}
				catch(Redirect redir) {
					throw redir;
				}
				catch(Exception e) {
					LOG.error("Approval failed.", e);
					Feedback.error("oauth2", "error.approvalFailed", e.getMessage());
				}
				
				throw new PageRedirect(pageCache.getHomePage());
			}

			throw new IllegalArgumentException("This type (" + responseType + ") of authorize is unsupported.");
		} catch(Redirect redir) {
			throw redir;
		} catch(IllegalArgumentException iae) {
			LOG.error("Invalid request.", iae);
			throw getErrorResponse(redirectUri, "invalid_request", iae.getMessage());			
		} catch(IllegalStateException iae) {
			LOG.error("Unauthorized client.", iae);
			throw getErrorResponse(redirectUri, "unauthorized_client", iae.getMessage());			
		} catch(UnauthorizedException | AccessDeniedException iae) {
			LOG.error("Access denied.", iae);
			throw getErrorResponse(redirectUri, "access_denied", iae.getMessage() == null ? "Access denied." : iae.getMessage()) ;			
		}  catch(Exception iae) {
			LOG.error("Fatal error.", iae);
			throw getErrorResponse(redirectUri, "server_error	", iae.getMessage());			
		}
	}

	private String scopesToUl(Set<OAuth2Scope> scopes) {
		var el = Html.ul();
		for(var scope : scopes) {
			el.appendChild(Html.ul(i18nService.format(scope.getBundle(), Locale.getDefault(), scope.getId() + ".name")));
		}
		return el.toString();
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		var httpReq = Request.get();
		var httpSession = httpReq.getSession();
		var oauthRequest = OAuth2Request.get(httpSession).orElseThrow(() -> new IllegalStateException("Not OAuth2 request in authentication state."));

		/* TODO how do you get i18n (of right locale) in code? */
		
		if(!config.getObject(OAuth2Configuration.class).isRequireApplicationRegistration()) {			
			if(StringUtils.isBlank(oauthRequest.clientId())) {
				document.getElementById("detail").remove();
			}
		}
		else {
			document.getElementById("detail").html(
					oAuth2ApplicationService.findOr(oauthRequest.clientId()).orElseThrow(() -> new IllegalStateException(MessageFormat.format("Application {} cannot be found.", oauthRequest.clientId()))).getName());
		}
		
		var scopesEl = document.getElementById("scopes");
		for(var scope : oauthRequest.requestedScopes()) {
			var scopeImpl = oauth.getScope(scope);
			scopesEl.appendChild(Html.li().html(i18nService.format(
					scopeImpl.getBundle(), Locale.getDefault(),  scope + ".name")));
			scopeImpl.verifyPermissions(Optional.of(oauthRequest), getCurrentUser());
		}
	}

	private UriRedirect getErrorResponse(String redirectUri, String error,
			String message) throws UnsupportedEncodingException {
		return new UriRedirect(redirectUri + (redirectUri.indexOf('?') == -1 ? "?" : "&") + "error=" + error + "&error_description=" + URLEncoder.encode(message, "UTF-8"));
	}
}
