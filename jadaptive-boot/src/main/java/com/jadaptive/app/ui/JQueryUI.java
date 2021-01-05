package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class JQueryUI extends AbstractPageExtension {

	@Override
	public void process(Document document, Page page) {
		PageHelper.appendStylesheet(document, "/app/content/jquery/jquery-ui.min.css");
		PageHelper.appendScript(document, "/app/content/jquery/jquery-ui.min.js");
	}

	@Override
	public String getName() {
		return "jquery-ui";
	}

}