package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class Codemirror extends AbstractPageExtension {

	@Override
	public void process(Document document, Page page) {
		
		PageHelper.appendScript(document, "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.59.1/codemirror.min.js");
		PageHelper.appendStylesheet(document, "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.59.1/codemirror.min.css");
				
	}

	@Override
	public String getName() {
		return "codemirror";
	}

}