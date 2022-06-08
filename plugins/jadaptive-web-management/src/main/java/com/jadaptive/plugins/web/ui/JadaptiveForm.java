package com.jadaptive.plugins.web.ui;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Extension
public class JadaptiveForm extends AbstractPageExtension {

	@Override
	public void process(Document document, Element element, Page page) {
		PageHelper.appendScript(document, "/app/content/jadaptive-form.js");
	}

	@Override
	public String getName() {
		return "jadaptive-form";
	}
}
