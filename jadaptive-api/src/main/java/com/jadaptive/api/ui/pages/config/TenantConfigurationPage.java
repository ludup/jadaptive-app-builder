package com.jadaptive.api.ui.pages.config;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class TenantConfigurationPage extends ConfigurationPage {

	@Override
	public String getUri() {
		return "options";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		super.generateAuthenticatedContent(document);
	}

	@Override
	protected boolean isSystem() {
		return false;
	}


}
