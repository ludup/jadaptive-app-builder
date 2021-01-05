package com.jadaptive.app.ui;

import org.pf4j.Extension;

import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageDependencies;

@Extension
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
public class Dashboard extends AuthenticatedPage {

	@Override
	public String getUri() {
		return "dashboard";
	}

}
