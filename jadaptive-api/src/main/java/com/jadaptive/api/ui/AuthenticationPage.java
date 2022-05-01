package com.jadaptive.api.ui;

import java.io.FileNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.user.UserService;

public abstract class AuthenticationPage<T> extends HtmlPage implements FormProcessor<T> {

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
	
	@Override
	protected void generateContent(Document doc) throws FileNotFoundException {
	}
	
	protected abstract boolean doForm(Document document, AuthenticationState state, T form) throws AccessDeniedException;
	
	public final void processForm(Document document, T form) throws FileNotFoundException {
		
		try {
			
			AuthenticationState state = authenticationService.getCurrentState();
			
			if(doForm(document, state, form)) {
				throw new PageRedirect(pageCache.resolvePage(authenticationService.completeAuthentication(state)));
			}
			
			authenticationService.reportAuthenticationFailure(state);
    	
    	} catch(AccessDeniedException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		if(StringUtils.isNotBlank(e.getMessage())) {
    			Feedback.error(e.getMessage());
    		} else {
        		Feedback.error("userInterface","error.accessDenied");
    		}
    	} catch(ObjectNotFoundException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		Feedback.error("userInterface","error.invalidCredentials");
    	}
    	
    	

	}
	

}
