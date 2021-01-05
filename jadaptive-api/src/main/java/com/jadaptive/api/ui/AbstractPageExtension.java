package com.jadaptive.api.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;

public abstract class AbstractPageExtension implements PageExtension {

	@Override
	public void process(Document document, Page page) throws IOException {

	}
	
	@Override
	public String getResource() {
		return String.format("%s.html", getClass().getSimpleName());
	}

}
