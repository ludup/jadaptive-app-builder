package com.jadaptive.plugins.icon;

import java.util.Objects;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

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
import com.jadaptive.utils.PasswordEncryptionType;

@Page
@View(contentType = "text/html", paths = { "icon-password"})
@ClasspathResource
public class IconPassword extends AbstractPage {

	static Logger log = LoggerFactory.getLogger(IconPassword.class);
	
	public static final String ICON_PASSWORD_STATE = "iconPasswordState";
	public static final String ICON_PASSWORD= "iconPassword";
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private IconPasswordService passwordService; 

	@Out(methods = HTTPMethod.GET)
	Document get(@In Document content, @Form IconForm form) {
		
		AuthenticationState state = authenticationService.getCurrentState();
    	if(Objects.isNull(state.getUser())) {
    		throw new Redirect(authenticationService.resetAuthentication());
    	}
    	
    	if(!passwordService.hasCredentials(state.getUser())) {
    		if(Objects.isNull(state.getAttribute(ICON_PASSWORD_STATE))) {
    			state.setAttribute(ICON_PASSWORD_STATE, IconPasswordState.UNREGISTERED);
    		}
    	} else {
    		state.setAttribute(ICON_PASSWORD_STATE, IconPasswordState.REGISTERED);
    	}
    	
    	IconPasswordState currentState = (IconPasswordState) state.getAttribute(ICON_PASSWORD_STATE);
		switch(currentState) {
		case UNREGISTERED:
			content.selectFirst("#infoText").text("In the future we will ask you to select from a series of icons. Set this up now by choosing the most memorable icons to you.");
			break;
		case CONFIRM_UNREGISTERED:
			content.selectFirst("#infoText").text("That's perfect. Now select the same icons again to confirm we have the correct sequence.");
			break;
		case REGISTERED:
			content.selectFirst("#infoText").text("We need to authenticate you with your icon sequence");
			break;
		}
		
		authenticationService.decorateAuthenticationPage(content);
		return content;
	}
	 
    @Out(methods = HTTPMethod.POST)
    Document post(@In Document content, @Form IconForm form) {

    	AuthenticationState state = authenticationService.getCurrentState();
    	if(Objects.isNull(state.getUser())) {
    		throw new IllegalStateException("Icon password must be used after establishing the user");
    	}
    	
    	IconPasswordState currentState = (IconPasswordState) state.getAttribute(ICON_PASSWORD_STATE);
		switch(currentState) {
		case UNREGISTERED:
			state.setAttribute(ICON_PASSWORD, form.getIconPassword());
			state.setAttribute(ICON_PASSWORD_STATE, IconPasswordState.CONFIRM_UNREGISTERED);
			throw new Redirect(IconPassword.class);

		case CONFIRM_UNREGISTERED:
			String pendingPassword = (String) state.getAttribute(ICON_PASSWORD);
    		if(Objects.isNull(pendingPassword)) {
    			throw new IllegalStateException("Expected pending icon sequence in session");
    		}
    		
    		if(pendingPassword.equals(form.getIconPassword())) {
    			passwordService.setIconPassword(state.getUser(), pendingPassword, PasswordEncryptionType.PBKDF2_SHA512_100000);
    			throw new Redirect(authenticationService.completeAuthentication(state.getUser()));
    		} else {
    			state.setAttribute(ICON_PASSWORD_STATE, IconPasswordState.UNREGISTERED);
    			state.removeAttribute(ICON_PASSWORD);
    			throw new Redirect(IconPassword.class);
    		}
    		
		default:
			if(passwordService.verifyIconPassword(state.getUser(), form.getIconPassword())) {
				state.setAttribute(AuthenticationService.ALTERNATIVE_PASSWORD, form.getIconPassword());
	    		throw new Redirect(authenticationService.completeAuthentication(authenticationService.getCurrentState().getUser()));
	    	}
			
			authenticationService.reportAuthenticationFailure(state.getUser().getUsername());
			
			Request.response().setStatus(HttpStatus.FORBIDDEN.value());
			content.selectFirst("#feedback").append("<div class=\"alert alert-danger\">Bad password</div>");
			return content;
		}

    }

    interface IconForm {
    	String getIconPassword();
    }
}
