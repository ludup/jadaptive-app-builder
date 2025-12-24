package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Optional;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.auth.AuthenticationPolicy;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.pages.auth.OptionalAuthentication;
import com.jadaptive.api.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public abstract class AuthenticationPage<T> extends HtmlPage implements FormProcessor<T> {
	
	private static final String UNDECORATED = "authentication.undecorated";

	private static final String LOGIN_IDENTIFIER = "LoginPage";
	
	public static void setUndecorated(HttpSession session) {
		session.setAttribute(UNDECORATED, true);
	}
	
	public static boolean isUndecorated(HttpSession session) {
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

		doGenerateContent(doc); 
		
		Element actions = doc.selectFirst("#actions");
		if(Objects.nonNull(actions)) {
			AuthenticationState state = authenticationService.getCurrentState();
			if(state.canStartAgain()) {
				actions.appendChild(Html.a(state.getStartAgainURL())
						.addClass("text-decoration-none d-block")
						.appendChild(new Element("sup")
								.appendChild(Html.i18n("userInterface", "reset.text"))));
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
		
			if(state.canCancel()) {
				actions.appendChild(Html.a(state.getCancelURL())
					.addClass("text-decoration-none d-block")
					.appendChild(new Element("sup")
							.appendChild(Html.i18n(AuthenticationPolicy.RESOURCE_KEY, "cancel.text"))));
			}
		}
		
		
		
		Element form = doc.selectFirst("form");
		if(Objects.nonNull(form)) {
			
			if(!isAllowFormExternalRedirect()) {
				sessionUtils.addContentSecurityPolicy(Request.response(), "form-action", "self");
			}
			
			sessionUtils.setupFormCSRFFToken(Request.get(), LOGIN_IDENTIFIER, form);
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
			
			sessionUtils.verifySameSiteRequest(request, LOGIN_IDENTIFIER);
			
			if(doForm(document, state, form)) {
				log.info("{} form was COMPLETED", getClass().getSimpleName());
				throw authenticationService.completeAuthentication(state, Optional.of(this)).
							maybeAttachToSession(request, sessionUtils.getTimeout());
			}
			
			log.info("{} form was not completed", getClass().getSimpleName());
			
			Request.response().setStatus(HttpStatus.FORBIDDEN.value());

			if(!Feedback.isSet()) {
		    	Feedback.error("default", "error.invalidCredentials");
			}
    	} catch(AccessDeniedException e) {
    		log.error("REMOVEME:", e);
    		Feedback.error(e.getMessage());
    	} catch(ObjectNotFoundException e) {	
    		log.error("REMOVEME:", e);
    		Feedback.error("userInterface","error.invalidCredentials");
    	} catch (UnauthorizedException e) {
    		log.error("REMOVEME:", e);
    		Feedback.error("userInterface","error.invalidCredentials");
    	} finally {
    		if(!isAllowFormExternalRedirect()) {
				sessionUtils.addContentSecurityPolicy(Request.response(), "form-action", "self");
			}
			
			sessionUtils.setupFormCSRFFToken(Request.get(), LOGIN_IDENTIFIER, document.selectFirst("form"));
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
