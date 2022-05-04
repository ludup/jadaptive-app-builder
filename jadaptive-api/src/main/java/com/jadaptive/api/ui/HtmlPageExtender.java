package com.jadaptive.api.ui;

import org.jsoup.nodes.Document;
import org.pf4j.ExtensionPoint;

public interface HtmlPageExtender extends ExtensionPoint {

	boolean isExtending(Page page);

	void processEnd(Document doc);

	void processStart(Document doc);
}
