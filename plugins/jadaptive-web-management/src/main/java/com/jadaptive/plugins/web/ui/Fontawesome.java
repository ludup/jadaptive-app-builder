package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class Fontawesome extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		PageHelper.appendStylesheet(document, "/app/content/fontawesome-pro-6.1.1-web/css/all.css");
		
	}

	@Override
	public String getName() {
		return "fontawesome";
	}

}