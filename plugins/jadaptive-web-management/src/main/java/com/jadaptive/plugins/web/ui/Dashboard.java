package com.jadaptive.plugins.web.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Dashboard extends AuthenticatedPage {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private PageCache pageCache;
	
	@Override
	protected void generateAuthenticatedContent(Document document) throws IOException {

		try {
			permissionService.assertAdministrator();
		} catch(AccessDeniedException ex) {
			throw new PageRedirect(pageCache.getHomePage());
		}
		super.generateAuthenticatedContent(document);
		
		

	}

	@Override
	public String getUri() {
		return "dashboard";
	}

}
