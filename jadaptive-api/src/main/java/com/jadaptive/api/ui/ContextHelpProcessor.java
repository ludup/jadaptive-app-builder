package com.jadaptive.api.ui;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public interface ContextHelpProcessor {
	 void process(Document document, Element element, Page page) throws IOException;
}