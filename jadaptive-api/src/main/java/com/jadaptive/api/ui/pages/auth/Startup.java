package com.jadaptive.api.ui.pages.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Startup extends HtmlPage {

	static Logger log = LoggerFactory.getLogger(Startup.class);
	
	@Override
	public String getUri() {
		return "startup";
	}
}
