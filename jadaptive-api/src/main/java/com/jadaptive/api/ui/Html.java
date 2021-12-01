package com.jadaptive.api.ui;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class Html {

	public static Element a(String href, String classes) {
		return new Element("a").attr("href", href).addClass(classes);
	}
	
	public static Node i(String classes) {
		return new Element("i").addClass(classes);
	}
	
	public static Element option(String value, String classes) {
		return new Element("option").val(value).addClass(classes);
	}
	
	public static Element option(String value, String text, String classes) {
		return new Element("option").val(value).addClass(classes).html(text);
	}
	
	public static Element option(String id, String value, String text, String classes) {
		return option(value, text, classes).attr("id", id);
	}
	
	public static Element span(String text) {
		return new Element("span").text(text);
	}
	
	public static Element span(String text, String classes) {
		return span(text).addClass(classes);
	}
}
