package com.jadaptive.plugins.keys;

import org.pf4j.Extension;

import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.user.User;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"})
@ModalPage
public class GeneratePersonalKey extends GeneratePublicKey{

	@Override
	public String getUri() {
		return "generate-key";
	}

	@Override
	protected User getUser() {
		return getCurrentUser();
	}

	@Override
	protected String getAction() {
		return "/generate/personal/key";
	}

	
}
