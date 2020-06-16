package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Form;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Page
@View(contentType = "text/html", paths = { "/changePassword"})
@Resource
public class ChangePassword extends AuthenticatedView {

	@Autowired
	private UserService userService; 

	@Autowired
	private SessionUtils sessionUtils; 
	
    @Out(methods = HTTPMethod.POST)
    Document service(@In Document content, @Form PasswordForm form) {
    	
    	Session session = sessionUtils.getActiveSession(Request.get());
    	User user = session.getUser();
    	userService.changePassword(user, form.getPassword().toCharArray(), false);
    	
    	/**
    	 * Show feedback instead of redirect
    	 */
    	throw new Redirect(Dashboard.class);
    }
    
    protected boolean isModal() {
    	return true;
    }

    public interface PasswordForm {

	String getPassword();
	String getConfirmPasssord();
    }

	@Override
	protected void onCreated() throws FileNotFoundException {
		
	}
}
