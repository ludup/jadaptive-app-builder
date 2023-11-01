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
	public static void appendHeadScript(Document document, String uri) {
		appendHeadScript(document, uri, false);
	}
	
	public static void appendHeadScript(Document document, String uri, boolean async) {
		appendHeadScript(document, uri, async, null);
	}
	public static void appendHeadScript(Document document, String uri, boolean async, String id) {
		Element head = PageHelper.getOrCreateTag(document, "head");
		
		for(Element e : head.getElementsByTag("script")) {
			if(uri.equals(e.attr("src"))) {
				return;
			}
		}
		Element e = new Element("script")
				.attr("src", uri)
				.attr("async", async)
				.attr("type", "text/javascript");
		if(Objects.nonNull(id)) {
			e.attr("id", id);
		}
		PageHelper.appendLast(head, "script", e);
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
		appendStylesheet(document, uri, id, "screen");
	}
	
	public static void appendStylesheet(Document document, String uri, String id, String media) {
		Element head = PageHelper.getOrCreateTag(document, "head");
		
		for(Element e : head.getElementsByTag("link")) {
			if(uri.equals(e.attr("href"))) {
				if(e.attr("media") == media) {
					return;
				}
			}
		}
		
		PageHelper.appendLast(head, "link", new Element("link")
				.attr("id", id)
				.attr("href", uri)
				.attr("media", media)
				.attr("rel", "stylesheet"));
	}

	public static Element createAnchor(String href, String text) {
		return new Element("a")
				.attr("href", href)
				.text(text);
	}
	
	public static Element createAnchor(String href) {
		return new Element("a")
				.attr("href", href);
	}
	
	public static void appendBodyScriptSnippet(Document document, String script) {

		String nonce = Utils.generateRandomAlphaNumericString(32);
		document.selectFirst("body").appendChild(new Element("script")
				.attr("nonce", nonce)
				.attr("type", "application/javascript")
				.text(script));

		ApplicationServiceImpl.getInstance().getBean(SessionUtils.class).addScriptNoncePolicy(Request.response(), nonce);
	}
	
	public static void appendHeadScriptSnippet(Document document, String script) {

		String nonce = Utils.generateRandomAlphaNumericString(32);
		document.selectFirst("head").appendChild(new Element("script")
				.attr("nonce", nonce)
				.attr("type", "application/javascript")
				.text(script));

		ApplicationServiceImpl.getInstance().getBean(SessionUtils.class).addScriptNoncePolicy(Request.response(), nonce);
	}
	
	public static void addContentSecurityPolicy(String policy, String value) {
		ApplicationServiceImpl.getInstance().getBean(SessionUtils.class).addContentSecurityPolicy(Request.response(), policy, value);
	}

}
