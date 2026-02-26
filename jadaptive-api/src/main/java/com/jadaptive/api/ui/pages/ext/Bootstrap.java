package com.jadaptive.api.ui.pages.ext;

import static com.jadaptive.utils.Npm.scripts;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.Page;

@Component
public class Bootstrap extends BootstrapOnly {

	static Logger log = LoggerFactory.getLogger(Bootstrap.class);

	@Override
	public void process(Document document, Element element, Page page) {
	
		super.process(document, element, page);
		
		scripts(document, "bootbox", "dist/bootbox.all.min.js");

	}

	@Override
	public String getName() {
		return "bootstrap";
	}

}
