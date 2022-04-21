package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.NamePairValue;

public class CollectionSearchFormInput {

	Element table;
	String url;
	String searchField;
	String idField;
	protected ObjectTemplate template;
	protected OrderedField field;
	
	public CollectionSearchFormInput(ObjectTemplate template, OrderedField field, String url, String searchField, String idField) {
		this.template = template;
		this.field = field;
		this.url = url;
		this.searchField = searchField;
		this.idField = idField;
	}

	public void renderInput(OrderedView panel, Element rootElement, 
			Collection<NamePairValue> selectedValues,
			boolean nameIsResourceKey) {
		
		rootElement.appendChild(new Element("div").addClass("row mb-3 collectionSearchInput")
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
							.attr("data-display", "static")
							.addClass("form-control collectionSearchInputText")
							.attr("data-bs-toggle", "dropdown")
							.attr("autocomplete", "off")
							.attr("data-url", url)
							.attr("data-field", searchField)
							.attr("data-id", idField)
							.attr("type", "text")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false"))
					.appendChild(new Element("span")
							.attr("class", "input-group-text")
						.appendChild(new Element("i")
								.attr("class", "fas fa-search")))
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
		for(NamePairValue value : selectedValues) {
			Element displayName;
			table.appendChild(new Element("tr")
					.appendChild(new Element("input")
							.attr("type", "hidden")
							.attr("name", field.getResourceKey())
							.attr("value", value.getValue()))
					.appendChild(new Element("td")
							.appendChild(displayName = Html.span(value.getName(), "underline"))
					.appendChild(new Element("td")
//							.appendChild(Html.a("#", "collectionSearchUp")
//									.appendChild(Html.i("far", "fa-fw", "fa-arrow-up", "me-2")))
//							.appendChild(Html.a("#", "collectionSearchDown")
//									.appendChild(Html.i("far", "fa-fw", "fa-arrow-down", "me-2")))
							.appendChild(Html.a("#", "collectionSearchDelete")
									.appendChild(Html.i("far", "fa-fw", "fa-trash", "me-2"))))));			
			if(nameIsResourceKey) {
				displayName.attr("jad:bundle", field.getBundle())
							.attr("jad:i18n", value.getName());
			}
		}
	}

}
