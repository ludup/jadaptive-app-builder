package com.jadaptive.api.ui.pages.ext;

import static com.jadaptive.utils.Npm.scripts;
import static com.jadaptive.utils.Npm.stylesheets;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;

@Component
public class BootstrapTable extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		scripts(document, "bootstrap-table", "bootstrap-table.min.js");
		scripts(document, "bootstrap-table", "extensions/mobile/bootstrap-table-mobile.min.js");
		stylesheets(document, "bootstrap-table", "bootstrap-table.min.css");
	}

	@Override
	public String getName() {
		return "bootstrapTable";
	}

}
