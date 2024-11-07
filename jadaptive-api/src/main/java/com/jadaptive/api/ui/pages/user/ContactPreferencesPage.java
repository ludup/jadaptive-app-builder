package com.jadaptive.api.ui.pages.user;

import org.springframework.stereotype.Component;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@RequestPage(path = "contact-preferences")
@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n" })
public class ContactPreferencesPage extends ObjectTemplatePage {

	@Override
	public String getUri() {
		return "contact-preferences";
	}

	@Override
	public FieldView getScope() {
		return FieldView.UPDATE;
	}

	@Override
	public String getResourceKey() {
		return UserContactPreferences.RESOURCE_KEY;
	}
	
	protected void assertPermissions() {
		// Handled by system
	}

}
