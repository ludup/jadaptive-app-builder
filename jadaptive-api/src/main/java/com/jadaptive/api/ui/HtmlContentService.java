package com.jadaptive.api.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;

public interface HtmlContentService {

	final String DEVELOPER_TOOLS = "Developer Tools";
	
	void resolveStylesheet(String uri, Document document, PageResources ext);

	void resolveScript(String uri, Document document, PageResources ext);

	Document resolveDocument(Class<?> clz, PageResources page, String resource, boolean canFail) throws IOException;

}
