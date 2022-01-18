package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class Bootstrap extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		PageHelper.appendScript(document, "/app/content/bootstrap-5.1.3-dist/js/bootstrap.bundle.min.js");
		
		PageHelper.appendScript(document, "/app/content/bootbox/bootbox.min.js");
		PageHelper.appendStylesheet(document, "/app/content/bootbox/bootbox-bootstrap5.css");
		PageHelper.appendStylesheet(document, "/app/content/bootstrap-5.1.3-dist/css/bootstrap.min.css");

	}

	@Override
	public String getName() {
		return "bootstrap";
	}

}
