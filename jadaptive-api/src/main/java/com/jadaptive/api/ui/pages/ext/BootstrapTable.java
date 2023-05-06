package com.jadaptive.api.ui.pages.ext;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Component
public class BootstrapTable extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		PageHelper.appendHeadScript(document, "/app/content/bootstrap-table/bootstrap-table.min.js");
		PageHelper.appendHeadScript(document, "/app/content/bootstrap-table/extensions/mobile/bootstrap-table-mobile.min.js");

		PageHelper.appendStylesheet(document, "/app/content/bootstrap-table/bootstrap-table.min.css");
	}

	@Override
	public String getName() {
		return "bootstrapTable";
	}

}
