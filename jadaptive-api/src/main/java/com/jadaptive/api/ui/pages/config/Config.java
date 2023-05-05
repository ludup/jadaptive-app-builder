package com.jadaptive.api.ui.pages.config;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.pages.ObjectTemplatePage;

@Component
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

	@Override
	protected void documentComplete(Document document) throws IOException {
		super.documentComplete(document);
	
		if(!template.isUpdatable()) {
			document.selectFirst("#saveButton").remove();
		}		
	}
	
	
}
