package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class JQuery extends AbstractPageExtension {

	@Override
	public void process(Document document, Page page) {
		PageHelper.appendScript(document, "/app/content/jquery/jquery-3.5.1.min.js");
	}

	@Override
	public String getName() {
		return "jquery";
	}

}