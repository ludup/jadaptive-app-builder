package com.jadaptive.app.ui;

import org.pf4j.Extension;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Extension
@RequestPage(path = "view/{resourceKey}/{uuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class ReadOnly extends ObjectPage {
	
	@Override
	public FieldView getScope() {
		return FieldView.READ;
	}

	@Override
	public String getUri() {
		return "view";
	}
}
