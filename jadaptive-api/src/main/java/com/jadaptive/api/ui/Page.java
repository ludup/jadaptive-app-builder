package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

public interface Page extends PageResources, ExtensionPoint {

	default void doGet(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		throw new UnsupportedOperationException();
	}

	default void doPost(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		throw new UnsupportedOperationException();
	}

	String getUri();
	
	String getResourcePath();

	default void onCreate() throws FileNotFoundException { }

	void injectHtmlSection(Document document, Element element, PageExtension ext) throws IOException;

	void injectHtmlSection(Document document, Element element, Class<?> clz, String resource) throws IOException;;
}
