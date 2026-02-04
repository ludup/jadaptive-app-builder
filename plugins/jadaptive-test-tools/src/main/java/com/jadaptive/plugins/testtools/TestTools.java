package com.jadaptive.plugins.testtools;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.HtmlPageExtender;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;

@Component
public class TestTools implements HtmlPageExtender {

	@Override
	public boolean isExtending(Page page, String uri) {
		return true;
	}

	@Override
	public void processStart(Document doc, String uri, Page page) throws FileNotFoundException {
		PageHelper.appendHeadScript(doc, "/app/content/js/test-tools-header.js");
	}

	@Override
	public void processEnd(Document doc, String uri, Page page) throws FileNotFoundException {
		PageHelper.appendBodyScript(doc, "/app/content/js/test-tools.js");
	}
}
