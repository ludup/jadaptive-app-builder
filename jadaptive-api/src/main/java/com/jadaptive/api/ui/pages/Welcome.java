package com.jadaptive.api.ui.pages;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Welcome extends HtmlPage {

	@Override
	public String getUri() {
		return "welcome";
	}

}
