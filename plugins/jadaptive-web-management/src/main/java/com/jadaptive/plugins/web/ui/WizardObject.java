package com.jadaptive.plugins.web.ui;

import org.pf4j.Extension;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

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
