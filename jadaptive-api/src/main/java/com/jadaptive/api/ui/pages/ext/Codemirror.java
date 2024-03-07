package com.jadaptive.api.ui.pages.ext;

import static com.jadaptive.utils.Npm.scripts;
import static com.jadaptive.utils.Npm.stylesheets;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;

@Component
public class Codemirror extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		scripts(document, "codemirror", "lib/codemirror.js");
		stylesheets(document, "codemirror", "lib/codemirror.css");
	}

	@Override
	public String getName() {
		return "codemirror";
	}

}