package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.ui.Html;

public class CollectionTextFormInput {

	Element table;
	protected ObjectTemplate template;
	protected OrderedField field;
	
	public CollectionTextFormInput(ObjectTemplate template, OrderedField field) {
		this.template = template;
		this.field = field;
	}

	public void renderInput(OrderedView panel, Element rootElement, 
			Collection<String> selectedValues) {
		
		rootElement.appendChild(new Element("div").addClass("row mb-3 collectionTextInput")
				.attr("data-resourcekey", field.getResourceKey())
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("div")
						.attr("id", String.format("%sDropdown", field.getResourceKey()))
						.attr("style", "position: relative")
						.addClass("input-group")
						.addClass("dropdown")
					.appendChild(new Element("input")
							.attr("id", String.format("%sText", field.getResourceKey()))
							.addClass("form-control collectionTextInputText")
							.attr("autocomplete", "off")
							.attr("type", "text"))
					.appendChild(new Element("span")
							.addClass("input-group-text collectionTextAdd")
						.appendChild(new Element("i")
								.attr("class", "fas fa-plus")))
					.appendChild(new Element("div")
							.addClass("dropdown-menu")
							.attr("aria-labelledby", String.format("%sDropdown", field.getResourceKey()))))
				.appendChild(new Element("div")
						.addClass("row mt-3")
					.appendChild(new Element("div")
							.attr("id", field.getFormVariable())
							.addClass("col-md-12")
							.appendChild(new Element("table")
									.addClass("w-100 collectionSearchTarget table table-sm table-striped")
								.appendChild(new Element("thead")
										.appendChild(new Element("tr")
												.appendChild(new Element("td")
														.attr("jad:bundle","default")
														.attr("jad:i18n", "name.name"))
												.appendChild(new Element("td")
														.attr("jad:bundle","default")
														.attr("jad:i18n", "actions.name"))
										.appendChild(table = new Element("tbody"))))
							)))
					.appendChild(new Element("div")
							.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-md-10")
								.appendChild(new Element("small")
										.addClass("text-muted")
										.attr("jad:bundle", field.getBundle())
										.attr("jad:i18n", String.format("%s.desc", field.getResourceKey()))))	
								)));
		for(String value : selectedValues) {
			table.appendChild(new Element("tr")
					.appendChild(new Element("input")
							.attr("type", "hidden")
							.attr("name", field.getResourceKey())
							.attr("value", value))
					.appendChild(new Element("td")
							.appendChild(Html.span(value, "underline"))
					.appendChild(new Element("td")
//							.appendChild(Html.a("#", "collectionSearchUp")
//									.appendChild(Html.i("far", "fa-fw", "fa-arrow-up", "me-2")))
//							.appendChild(Html.a("#", "collectionSearchDown")
//									.appendChild(Html.i("far", "fa-fw", "fa-arrow-down", "me-2")))
							.appendChild(Html.a("#", "collectionSearchDelete")
									.appendChild(Html.i("far", "fa-fw", "fa-trash", "me-2"))))));			
		}
	}

}
