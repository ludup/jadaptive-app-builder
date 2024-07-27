package com.jadaptive.api.avatar;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@ModalPage
@PageProcessors(extensions = { "help", "i18n" })
public class AvatarUploadPage extends AuthenticatedPage {

	@Override
	public String getUri() {
		return "avatar-upload";
	}
}
