package com.jadaptive.api.ui.wizards;

import org.pf4j.Extension;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class WizardObject extends ObjectTemplatePage {
	
	String wizardKey;
	
	public String getWizardKey() {
		return wizardKey;
	}

	@Override
	public FieldView getScope() {
		return FieldView.CREATE;
	}

	@Override
	public String getUri() {
		return "wizard-object";
	}
}
