package com.jadaptive.api.ui.pages.user;

import java.util.Objects;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.pages.user.ChangeMyPassword.PasswordForm;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@ModalPage
@PageProcessors(extensions = { "i18n" })
public class ChangeMyPassword extends AuthenticatedPage implements FormProcessor<PasswordForm>{

	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private UserService userService;
	
	@Override
	public String getUri() {
		return "change-my-password";
	}

	public void processForm(Document document, PasswordForm form) {
		Session session = sessionUtils.getActiveSession(Request.get());
		if(Objects.isNull(session)) {
			
		}
		User user = session.getUser();
		
		if(!userService.verifyPassword(user, form.getCurrentPassword().toCharArray())) {
			Feedback.error("default", "error.incorrectPassword");
			return;
		}
		
		try {
			userService.changePassword(user, form.getNewPassword().toCharArray(), false);
			Feedback.success("default", "success.passwordChanged");
			
		} catch(IllegalStateException e) {
			Feedback.error(e.getMessage());
		}

	}
	public interface PasswordForm {

		String getCurrentPassword();
		String getNewPassword();
		String getConfirmPasssord();
	}

	@Override
	public Class<PasswordForm> getFormClass() {
		return PasswordForm.class;
	}

}
