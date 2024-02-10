package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.user.UserService;

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
	
	Class<T> formClass;
	
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
	
	@Override
	protected final void generateContent(Document doc) throws FileNotFoundException {
		var req = Request.get();
		try {
			sessionUtils.getSession(req);
			throw new UriRedirect();
		} catch (UnauthorizedException e) {
			
			doGenerateContent(doc);
		}
		
		if(isUndecorated(req.getSession())) {
			try {
				var cardBody = doc.getElementsByClass("card-body").first();
				var loginContainer = doc.getElementById("login-container");
				var par = loginContainer.parent();
				loginContainer.remove();
				cardBody.children().forEach(par::appendChild);
				doc.getElementsByTag("footer").forEach(e -> e.remove());
				doc.getElementsByTag("header").forEach(e -> e.remove());
			}
			catch(Exception e) {
				log.warn(MessageFormat.format("The session requested an undecorated login, but {0} does not support it.", getClass().getName()));
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
		
			sessionUtils.verifySameSiteRequest(Request.get());
			
			if(doForm(document, state, form)) {
				throw new PageRedirect(pageCache.resolvePage(authenticationService.completeAuthentication(state, Optional.of(this))));
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
		
		throw new PageRedirect(pageCache.resolvePage(authenticationService.getCurrentState().getCurrentPage()));
	}

	@Override
	protected void afterProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		authenticationService.decorateAuthenticationPage(getCurrentDocument());
	}
	
	
	
}
