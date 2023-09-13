package com.jadaptive.api.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

public interface PageExtension extends PageResources, ExtensionPoint {

	void process(Document document, Element extensionElement, Page page) throws IOException;

	String getName();
}
