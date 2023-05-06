package com.jadaptive.api.ui.pages.ext;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Component
public class Bootstrap extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		PageHelper.appendHeadScript(document, "/app/content/bootstrap-5.2.2-dist/js/bootstrap.bundle.min.js");
		
		PageHelper.appendHeadScript(document, "/app/content/bootbox/bootbox.all.min.js");
		PageHelper.appendStylesheet(document, "/app/content/bootstrap-5.2.2-dist/css/bootstrap.min.css");

	}

	@Override
	public String getName() {
		return "bootstrap";
	}

}
