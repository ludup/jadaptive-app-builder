package com.jadaptive.plugins.web.ui;

import org.pf4j.Extension;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Extension
@RequestPage(path = "config/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class Config extends ObjectTemplatePage {
	
	@Override
	public FieldView getScope() {
		return FieldView.UPDATE;
	}

	@Override
	public String getUri() {
		return "config";
	}
}