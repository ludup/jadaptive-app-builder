package com.jadaptive.api.ui.pages.ext;

import static com.jadaptive.utils.Npm.scripts;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;

@Component
public class JQuery extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		scripts(document, "jquery", "dist/jquery.min.js");
	}

	@Override
	public String getName() {
		return "jquery";
	}

}