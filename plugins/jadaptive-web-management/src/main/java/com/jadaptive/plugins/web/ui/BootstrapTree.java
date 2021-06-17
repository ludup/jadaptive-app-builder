package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class BootstrapTree extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		PageHelper.appendScript(document, "/app/content/bootstrap-tree/js/bstreeview.min.js");
		PageHelper.appendStylesheet(document, "/app/content/bootstrap-tree/css/bstreeview.min.css");
	}

	@Override
	public String getName() {
		return "bootstrap-tree";
	}

}
