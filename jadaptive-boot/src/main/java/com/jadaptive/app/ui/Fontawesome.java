package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class Fontawesome extends AbstractPageExtension {

	@Override
	public void process(Document document, Page page) {
		PageHelper.appendStylesheet(document, "/app/content/fontawesome-pro-5.15.1-web/css/all.css");
		
	}

	@Override
	public String getName() {
		return "fontawesome";
	}

}