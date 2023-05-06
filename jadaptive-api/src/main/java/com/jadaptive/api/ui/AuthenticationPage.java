package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.util.Objects;

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
		
		try {
			sessionUtils.getSession(Request.get());
			throw new UriRedirect();
		} catch (UnauthorizedException e) {
			
			doGenerateContent(doc);
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
				throw new PageRedirect(pageCache.resolvePage(authenticationService.completeAuthentication(state)));
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
		
		authenticationService.reportAuthenticationFailure(state);
		
		throw new PageRedirect(pageCache.resolvePage(authenticationService.getCurrentState().getCurrentPage()));
	}
	
}
