package com.jadaptive.plugins.web.ui;

import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.web.ui.ChangeMyPassword.PasswordForm;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@ModalPage
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
			document.selectFirst("#feedback").appendChild(new Element("div")
					.addClass("alert alert-danger")
					.appendChild(new Element("i")
							.addClass("far fa-exclamation-square"))
					.appendChild(new Element("span")
							.text("Incorrect password")));
			return;
		}
		
		userService.changePassword(user, form.getNewPassword().toCharArray(), false);
		
		document.selectFirst("#feedback").appendChild(new Element("div")
				.addClass("alert alert-success")
				.appendChild(new Element("i")
						.addClass("far fa-thumbs-up"))
				.appendChild(new Element("span")
						.text("Password changed")));

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
