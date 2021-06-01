package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;

import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.user.User;
import com.jadaptive.plugins.web.ui.ChangePassword.PasswordForm;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@ModalPage
public class ChangePassword extends AuthenticationPage<PasswordForm> {

	public ChangePassword() {
		super(PasswordForm.class);
	}

	@Override
	public String getUri() {
		return "change-password";
	}

	public boolean doForm(Document document, AuthenticationState state, PasswordForm form) throws AccessDeniedException {

		Session session = sessionUtils.getActiveSession(Request.get());
		User user = session.getUser();
		userService.changePassword(user, form.getPassword().toCharArray(), false);
		return true;
	}

	public interface PasswordForm {

		String getPassword();
		String getConfirmPasssord();
	}

}
