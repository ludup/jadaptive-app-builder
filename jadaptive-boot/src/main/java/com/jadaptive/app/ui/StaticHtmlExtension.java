package com.jadaptive.app.ui;

import java.io.IOException;

import org.apache.commons.lang.WordUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageExtension;

public class StaticHtmlExtension implements PageExtension {

	String id;
	
	String htmlResource;
	
	public StaticHtmlExtension(String id) {
		this.id = id;
		this.htmlResource = WordUtils.capitalize(id) + ".html";
	}
	
	public StaticHtmlExtension(String id, String htmlResource) {
		this.id = id;
		this.htmlResource = htmlResource;
	}
	
	@Override
	public String getHtmlResource() {
		return htmlResource;
	}

	@Override
	public String getCssResource() {
		return null;
	}

	@Override
	public String getJsResource() {
		return null;
	}

	@Override
	public void process(Document document, Element extensionElement, Page page) throws IOException {
		
	}

	@Override
	public String getName() {
		return id;
	}

}
