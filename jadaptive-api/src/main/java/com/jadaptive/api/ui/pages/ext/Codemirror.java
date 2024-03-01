package com.jadaptive.api.ui.pages.ext;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Component
public class Codemirror extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		
		PageHelper.appendHeadScript(document, "/app/content/npm2mvn/npm/codemirror/current/lib/codemirror.js");
		PageHelper.appendStylesheet(document, "/app/content/npm2mvn/npm/codemirror/current/lib/codemirror.css");
				
	}

	@Override
	public String getName() {
		return "codemirror";
	}

}