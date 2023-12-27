package com.jadaptive.api.ui;

import org.jsoup.nodes.Element;

import com.jadaptive.utils.Utils;

public class Html {

	public static Element a(String href, String classes) {
		return a(href).addClass(classes);
	}
	
	public static Element a(String href, String... classes) {
		return a(href).addClass(Utils.csv(" ", classes));
	}
	
	public static Element a(String href) {
		return new Element("a").attr("href", href);
	}
	
	public static Element i(String classes) {
		return new Element("i").addClass(classes);
	}
	
	public static Element i(String... classes) {
		return new Element("i").addClass(Utils.csv(" ", classes));
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
	
	public static Element i18n(String bundle, String i18n, Object... args) {
		Element el = new Element("span").attr("jad:bundle", bundle).attr("jad:i18n", i18n);
		for(int i=0;i<args.length;i++) {
			if(args[i]!=null) {
				el.attr(String.format("jad:arg%d",i), args[i].toString());
			}
		}
		return el;
	}
	
	public static Element i18nTag(String tag, String bundle, String i18n, Object... args) {
		Element el = new Element(tag).attr("jad:bundle", bundle).attr("jad:i18n", i18n);
		for(int i=0;i<args.length;i++) {
			if(args[i]!=null) {
				el.attr(String.format("jad:arg%d",i), args[i].toString());
			}
		}
		return el;
	}
	
	public static Element p(String bundle, String i18n, Object... args) {
		return i18nTag("p", bundle, i18n, args);
	}
	
	public static Element h1(String bundle, String i18n, Object... args) {
		return i18nTag("h1", bundle, i18n, args);
	}
	
	public static Element h2(String bundle, String i18n, Object... args) {
		return i18nTag("h2", bundle, i18n, args);
	}
	
	public static Element h3(String bundle, String i18n, Object... args) {
		return i18nTag("h3", bundle, i18n, args);
	}
	
	public static Element h4(String bundle, String i18n, Object... args) {
		return i18nTag("h4", bundle, i18n, args);
	}
	
	public static Element h5(String bundle, String i18n, Object... args) {
		return i18nTag("h5", bundle, i18n, args);
	}
	
	public static Element h6(String bundle, String i18n, Object... args) {
		return i18nTag("h6", bundle, i18n, args);
	}
	
	public static Element span(String text, String... classes) {
		return span(text).addClass(Utils.csv(" ", classes));
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

	public static Element text(String id, String name, String value, String... classes) {
		return new Element("input").attr("type", "text")
				.attr("id", id)
				.attr("name", name)
				.val(value)
				.addClass(Utils.csv(" ", classes));
	}

	public static Element div(String... classes) {
		return new Element("div").addClass(Utils.csv(" ", classes));
	}

	public static Element table(String... classes) {
		return new Element("table").addClass(Utils.csv("", classes));
	}
	
	public static Element thead() {
		return new Element("thead");
	}
	
	public static Element tr() {
		return new Element("tr");
	}
	
	public static Element td() {
		return new Element("td");
	}
	
	public static Element td(String... classes) {
		return td().addClass(Utils.csv("", classes));
	}

	public static Element tbody() {
		return new Element("tbody");
	}

	public static Element input(String type, String name, String value) {
		return new Element("input").attr("type", type).attr("name", name).val(value);
	}

	public static Element p(String text, String... classes) {
		return new Element("p").html(text).addClass(Utils.csv(" ", classes));
	}

	public static Element button(String... classes) {
		return new Element("button").addClass(Utils.csv(" ", classes));
	}

	public static Element img(String src, String... classes) {
		return new Element("img").attr("src", src).addClass(Utils.csv(" ", classes));
	}

	public static Element label(String bundle, String i18n, String... args) {
		Element el = new Element("label").attr("jad:bundle", bundle).attr("jad:i18n", i18n);
		for(int i=0;i<args.length;i++) {
			if(args[i]!=null) {
				el.attr(String.format("jad:arg%d",i), args[i].toString());
			}
		}
		return el;
	}

}
