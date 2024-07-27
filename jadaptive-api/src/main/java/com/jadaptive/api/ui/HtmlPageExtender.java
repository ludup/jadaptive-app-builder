package com.jadaptive.api.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.pf4j.ExtensionPoint;

public interface HtmlPageExtender extends ExtensionPoint {

	boolean isExtending(Page page, String uri);

	default void processEnd(Document doc, String uri, Page page) throws FileNotFoundException { };

	default void processStart(Document doc, String uri, Page page) throws FileNotFoundException { };

	default void generateContent(Document document, Page htmlPage) throws FileNotFoundException { };

	default void processPost(Document doc, Page htmlPage) throws FileNotFoundException { };
}
