package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.codesmith.webbits.Created;
import com.codesmith.webbits.Form;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.Bootstrapify;
import com.codesmith.webbits.extensions.Absolutify;
import com.codesmith.webbits.extensions.Enablement;
import com.codesmith.webbits.extensions.I18N;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.codesmith.webbits.fontawesome.FontAwesomeify;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.app.auth.AuthenticationService;

@Page({ Bootstrapify.class, FontAwesomeify.class, 
	PageResources.class, 
	PageResourcesElement.class, 
	Absolutify.class, 
	Enablement.class, I18N.class })
@View(contentType = "text/html", paths = { "/login"})
@Resource
public class Login extends AbstractPage {

	@Autowired
	private UserService userService; 
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionUtils sessionUtils; 
	
	@Created
	void created() throws FileNotFoundException {
		if(sessionUtils.hasActiveSession(Request.get())) {
			throw new Redirect(Dashboard.class);
		}
	}
	
    @Out(methods = HTTPMethod.POST)
    Document service(@In Document content, @Form LoginForm form) {
	
    	try {
	    	Session session = authenticationService.logonUser(form.getUsername(),
	    			form.getPassword(), tenantService.getCurrentTenant(), 
	    			Request.get().getRemoteAddr(), 
	    			Request.get().getHeader(HttpHeaders.USER_AGENT));
	    	
	    	sessionUtils.addSessionCookies(Request.get(), Request.response(), session);
	    	
	    	User user = userService.getUser(session.getUsername());
	    	if(user.getPasswordChangeRequired()) {
	    		throw new Redirect(ChangePassword.class);
	    	}
	    	
	    	throw new Redirect(JadaptiveApp.class);
    	
    	} catch(AccessDeniedException | EntityNotFoundException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		content.selectFirst("#feedback").append("<div class=\"alert alert-danger\">Bad username or password</div>");
    		return content;
    	}
    }

    public interface LoginForm {
		String getUsername();
		String getPassword();
    }
}
