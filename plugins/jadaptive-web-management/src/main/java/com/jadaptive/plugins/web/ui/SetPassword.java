package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.ui.ErrorPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.web.ui.SetPassword.PasswordForm;

@Extension
@RequestPage(path = "set-password/{uuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n" })
@ModalPage
public class SetPassword extends HtmlPage implements FormProcessor<PasswordForm> {

	@Autowired
	private UserService userService;

	String uuid;
	
	public SetPassword() {
		
	}

	@Override
	public String getUri() {
		return "set-password";
	}
	
	public final void processForm(Document document, PasswordForm form) throws FileNotFoundException {
		
		if(StringUtils.isBlank(form.getPassword()) 
				|| StringUtils.isBlank(form.getConfirmPassword())) {
			Feedback.error("default", "passwordsEmpty.text");
			throw new UriRedirect("/app/ui/set-password/" + uuid);
		}
		
		if(!form.getPassword().equals(form.getConfirmPassword())) {
			Feedback.error("default", "passwordsDoNotMatch.text");
			throw new UriRedirect("/app/ui/set-password/" + uuid);
		}
		
		try {
			User user = userService.getObjectByUUID(uuid);
			userService.setPassword(user, form.getPassword().toCharArray(), form.getForceChange());
		
			Feedback.success("default", "passwordChanged.text", user.getUsername());
			
		} catch(Throwable e) {
			throw new PageRedirect(new ErrorPage(e));
		}
		
		throw new UriRedirect("/app/ui/search/users");

	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public interface PasswordForm {

		String getPassword();
		String getConfirmPassword();
		boolean getForceChange();
	}

	@Override
	public Class<PasswordForm> getFormClass() {
		return PasswordForm.class;
	}

}
