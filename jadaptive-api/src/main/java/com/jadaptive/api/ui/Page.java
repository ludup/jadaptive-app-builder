package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface Page extends PageResources, ExtensionPoint {

	default void doGet(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		throw new UnsupportedOperationException();
	}

	default void doPost(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		throw new UnsupportedOperationException();
	}

	String getUri();
	
	String getResourcePath();
	
	default boolean isThemePage() { return true; }

	default void onCreate() throws FileNotFoundException { }

	void injectHtmlSection(Document document, Element element, PageExtension ext) throws IOException;

	void injectHtmlSection(Document document, Element element, Class<?> clz, String resource, boolean canFail)
			throws IOException;;
			
    void addProcessor(PageExtension ext);

	Document generateHTMLDocument(String uri) throws IOException;

}
