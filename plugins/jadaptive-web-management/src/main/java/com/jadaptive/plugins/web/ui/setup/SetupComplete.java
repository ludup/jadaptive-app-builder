package com.jadaptive.plugins.web.ui.setup;

import org.pf4j.Extension;

import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Extension
@RequestPage(path="setup-complete")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class SetupComplete extends HtmlPage {

	@Override
	public String getUri() {
		return "setup-complete";
	}

}
