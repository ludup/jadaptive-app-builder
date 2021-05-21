package com.jadaptive.api.ui;

import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PageHelper {
	
	public static void appendLast(Element parent, String lastOfTag, Element node) {
		
		Elements elements = parent.select(lastOfTag);
		if(elements.isEmpty()) {
			parent.appendChild(node);
		} else {
			elements.last().after(node);
		}
	}

	public static Element getOrCreateTag(Document document, String tag) {
		
		Element element = document.selectFirst(tag);
		if(Objects.nonNull(element)) {
			return element;
		}
		return document.appendChild(new Element("head"));
	}
	
	public static void appendScript(Document document, String uri) {
		Element head = PageHelper.getOrCreateTag(document, "head");
		PageHelper.appendLast(head, "script", new Element("script").attr("src", uri).attr("type", "text/javascript"));
	}
	
	public static void appendStylesheet(Document document, String uri) {
		Element head = PageHelper.getOrCreateTag(document, "head");
		PageHelper.appendLast(head, "link", new Element("link").attr("href", uri).attr("rel", "stylesheet"));
	}

	public static Element createAnchor(String href, String text) {
		return new Element("a")
				.attr("href", href)
				.text(text);
	}

}