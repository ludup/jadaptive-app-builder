package com.jadaptive.plugins.web.ui;

import org.pf4j.Extension;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@Extension
@RequestPage(path = "system/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils", "jadaptive-form"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class System extends ObjectTemplatePage {
	
	@Override
	public FieldView getScope() {
		return FieldView.UPDATE;
	}

	@Override
	public String getUri() {
		return "system";
	}
}
