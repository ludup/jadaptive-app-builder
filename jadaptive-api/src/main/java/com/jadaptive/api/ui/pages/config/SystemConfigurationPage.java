package com.jadaptive.api.ui.pages.config;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n" } )
public class SystemConfigurationPage extends ConfigurationPage {

	@Autowired
	private PermissionService permissionService;
	
	@Override
	public String getUri() {
		return "systemConfiguration";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {

		permissionService.assertAdministrator();
		
		super.generateAuthenticatedContent(document);
	}

	@Override
	protected boolean isSystem() {
		return true;
	}

}
