package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class BootstrapTable extends AbstractPageExtension {

	@Override
	public void process(Document document, Page page) {
		PageHelper.appendScript(document, "/app/content/bootstrap-table/bootstrap-table.min.js");
		PageHelper.appendScript(document, "/app/content/bootstrap-table/extensions/mobile/bootstrap-table-mobile.min.js");

		PageHelper.appendStylesheet(document, "/app/content/bootstrap-table/bootstrap-table.min.css");
	}

	@Override
	public String getName() {
		return "bootstrapTable";
	}

}
