package com.jadaptive.api.ui;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.jadaptive.utils.Utils;

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

	public static Element li(String... classes) {
		return new Element("li").addClass(Utils.csv(" ", classes));
	}
	
	public static Element ul(String... classes) {
		return new Element("ul").addClass(Utils.csv(" ", classes));
	}

	public static Element nav() {
		return new Element("nav");
	}

	public static Node text(String id, String name, String value, String... classes) {
		return new Element("input").attr("type", "text")
				.attr("id", id)
				.attr("name", name)
				.val(value)
				.addClass(Utils.csv(" ", classes));
	}
}
