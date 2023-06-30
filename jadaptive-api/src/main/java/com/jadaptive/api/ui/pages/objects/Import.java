package com.jadaptive.api.ui.pages.objects;

import org.springframework.stereotype.Component;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@Component
@RequestPage(path = "import/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class Import extends ObjectTemplatePage {

	@Override
	public String getUri() {
		return "import";
	}

	@Override
	public FieldView getScope() {
		return FieldView.IMPORT;
	}
	

}
