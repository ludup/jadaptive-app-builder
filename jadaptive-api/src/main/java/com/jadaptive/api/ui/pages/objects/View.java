package com.jadaptive.api.ui.pages.objects;

import org.springframework.stereotype.Component;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@Component
@RequestPage(path = "view/{resourceKey}/{uuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class View extends ObjectTemplatePage {
	
	@Override
	public FieldView getScope() {
		return FieldView.READ;
	}

	@Override
	public String getUri() {
		return "view";
	}
}