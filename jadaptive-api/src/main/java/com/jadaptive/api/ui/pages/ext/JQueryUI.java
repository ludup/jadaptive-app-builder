package com.jadaptive.api.ui.pages.ext;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Component
public class JQueryUI extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		PageHelper.appendStylesheet(document, "/app/content/npm2mvn/npm/jquery/current/jquery-ui.min.css");
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/jquery/current/jquery-ui.min.js");
	}

	@Override
	public String getName() {
		return "jquery-ui";
	}

}