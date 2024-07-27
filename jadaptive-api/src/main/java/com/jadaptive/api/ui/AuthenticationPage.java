package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.pages.auth.OptionalAuthentication;
import com.jadaptive.api.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public abstract class AuthenticationPage<T> extends HtmlPage implements FormProcessor<T> {
	
	private static final String UNDECORATED = "authentication.undecorated";

	public static void setUndecorated(HttpSession session) {
		session.setAttribute(UNDECORATED, true);
	}
	
	private static boolean isUndecorated(HttpSession session) {
		return Boolean.TRUE.equals(session.getAttribute(UNDECORATED));
	}

	static Logger log = LoggerFactory.getLogger(AuthenticationPage.class);
	
	@Autowired
	protected SessionUtils sessionUtils;

	@Autowired
	protected AuthenticationService authenticationService;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	private PageCache pageCache;  
	
	private Class<T> formClass;
	
	protected AuthenticationPage(Class<T> formClass) {
		this.formClass = formClass;
	}
	
	public Class<T> getFormClass() {
		return formClass;
	}
	
	public String getIconGroup() {
		return "fa-solid";
	}
	
	public String getIcon() {
		return "fa-key";
	}
	
	protected boolean isRedirectInSession() {
		return true;
	}
	
	@Override
	protected final void generateContent(Document doc) throws FileNotFoundException {
		var req = Request.get();
		var session = Session.getOr(req);
		if(session.isPresent() && isRedirectInSession()) {
			throw new UriRedirect();
		}
		
		doGenerateContent(doc); 
		
		if(isUndecorated(req.getSession())) {
			try {
				var cardBody = doc.getElementsByClass("card-body").first();
				if(cardBody != null) {
					var loginContainer = Objects.requireNonNull(doc.getElementById("login-container"));
					var par =  Objects.requireNonNull(loginContainer.parent());
					loginContainer.remove();
					cardBody.children().forEach(par::appendChild);
					doc.getElementsByTag("footer").forEach(e -> e.remove());
					doc.getElementsByTag("header").forEach(e -> e.remove());
				}
			}
			catch(Exception e) {
				log.warn(MessageFormat.format("The session requested an undecorated login, but {0} does not support it.", getClass().getName()), e);
			}
			
		}
		
		Element actions = doc.selectFirst("#actions");
		if(Objects.nonNull(actions)) {
			AuthenticationState state = authenticationService.getCurrentState();
			if(state.canReset()) {
				actions.appendChild(Html.a("/app/api/reset-login")
						.addClass("text-decoration-none d-block")
						.appendChild(new Element("sup")
								.appendChild(Html.i18n("userInterface", "reset.text"))));
				
//				<a id="cancel" class="" href="/app/ui/login"><sup><span jad:bundle="userInterface" jad:i18n="cancel.text">Cancel</span></sup></a>	      
			}
			
			if(state.isRequiredAuthenticationComplete()
					&& !state.isOptionalComplete()
					&& state.getOptionalAvailable() > 1
					&& !OptionalAuthentication.class.equals(authenticationService.getCurrentPage())) {
				actions.appendChild(Html.a("/app/api/change-auth")
						.addClass("text-decoration-none d-block")
						.appendChild(new Element("sup")
								.appendChild(Html.i18n("userInterface", "changeAuthentication.text"))));
			}
		}
		
		Element form = doc.selectFirst("form");
		if(Objects.nonNull(form)) {
			
			if(!isAllowFormExternalRedirect()) {
				sessionUtils.addContentSecurityPolicy(Request.response(), "form-action", "self");
			}
			form.appendChild(Html.input("hidden", 
					SessionUtils.CSRF_TOKEN_ATTRIBUTE, 
						sessionUtils.setupCSRFToken(Request.get()))
						.attr("id", "csrftoken"));
		}
		

	}
	
	protected boolean isAllowFormExternalRedirect() {
		return false;
	}

	public abstract String getBundle();
	
	protected void doGenerateContent(Document doc) throws FileNotFoundException { }
	
	protected abstract boolean doForm(Document document, AuthenticationState state, T form) throws AccessDeniedException, FileNotFoundException;
	
	public final void processForm(Document document, T form) throws FileNotFoundException {
		
		
		AuthenticationState state = authenticationService.getCurrentState();
		
		try {
		
			var request = Request.get();
			
			sessionUtils.verifySameSiteRequest(request);
			
			if(doForm(document, state, form)) {
				throw authenticationService.completeAuthentication(state, Optional.of(this)).
							maybeAttachToSession(request, sessionUtils.getTimeout());
			}
    	
			Request.response().setStatus(HttpStatus.FORBIDDEN.value());
	    	Feedback.error("default", "error.invalidCredentials");
	    	
    	} catch(AccessDeniedException e) {
    		Feedback.error(e.getMessage());
    	} catch(ObjectNotFoundException e) {	
    		Feedback.error("userInterface","error.invalidCredentials");
    	} catch (UnauthorizedException e) {
    		Feedback.error("userInterface","error.invalidCredentials");
    	} 
		
		authenticationService.reportAuthenticationFailure(state, this);
		
		throw new PageRedirect(pageCache.resolvePage(authenticationService.getCurrentPage()));
	}

	@Override
	protected void afterProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		authenticationService.decorateAuthenticationPage(getCurrentDocument());
	}

	public abstract boolean canAuthenticate(AuthenticationState state);

	public abstract String getAuthenticatorUUID();
	
}
