package com.jadaptive.plugins.web.ui;

import org.pf4j.Extension;

import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Dashboard extends AuthenticatedPage {

	@Override
	public String getUri() {
		return "dashboard";
	}

}
