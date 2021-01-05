package com.jadaptive.api.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.pf4j.ExtensionPoint;

public interface PageExtension extends ExtensionPoint {

	void process(Document document, Page page) throws IOException;

	String getName();
	
	String getResource();

}
