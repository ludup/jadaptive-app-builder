package com.jadaptive.api.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public abstract class AbstractPageExtension implements PageExtension {

	@Override
	public void process(Document document, Element element, Page page) throws IOException {

	}
	
	@Override
	public String getHtmlResource() {
		return String.format("%s.html", getClass().getSimpleName());
	}
	
	@Override
	public String getCssResource() {
		return String.format("%s.css", getClass().getSimpleName());
	}
	
	@Override
	public String getJsResource() {
		return String.format("%s.js", getClass().getSimpleName());
	}

}
