package com.jadaptive.api.ui;

import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.utils.Utils;

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
		Element head = new Element("head");
		document.prependChild(head);
		return head;
	}
	
	public static void appendScript(Document document, String uri) {
		Element head = PageHelper.getOrCreateTag(document, "head");
		
		for(Element e : head.getElementsByTag("script")) {
			if(uri.equals(e.attr("src"))) {
				return;
			}
		}
		PageHelper.appendLast(head, "script", new Element("script").attr("src", uri).attr("type", "text/javascript"));
	}
	
	public static void appendStylesheet(Document document, String uri) {
		Element head = PageHelper.getOrCreateTag(document, "head");
		
		for(Element e : head.getElementsByTag("link")) {
			if(uri.equals(e.attr("href"))) {
				return;
			}
		}
		PageHelper.appendLast(head, "link", new Element("link").attr("href", uri).attr("rel", "stylesheet"));
	}
	
	public static void appendStylesheet(Document document, String uri, String id) {
		Element head = PageHelper.getOrCreateTag(document, "head");
		
		for(Element e : head.getElementsByTag("link")) {
			if(uri.equals(e.attr("href"))) {
				return;
			}
		}
		
		PageHelper.appendLast(head, "link", new Element("link").attr("id", id).attr("href", uri).attr("rel", "stylesheet"));
	}

	public static Element createAnchor(String href, String text) {
		return new Element("a")
				.attr("href", href)
				.text(text);
	}
	
	public static void appendScriptSnippet(Document document, String script) {

		String nonce = Utils.generateRandomAlphaNumericString(32);
		document.selectFirst("body").appendChild(new Element("script")
				.attr("nonce", nonce)
				.attr("type", "application/javascript")
				.text(script));

		ApplicationServiceImpl.getInstance().getBean(SessionUtils.class).addScriptNoncePolicy(Request.response(), nonce);
	}
	
	public static void addContentSecurityPolicy(String policy, String value) {
		ApplicationServiceImpl.getInstance().getBean(SessionUtils.class).addContentSecurityPolicy(Request.response(), policy, value);
	}

}
