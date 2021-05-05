package com.jadaptive.app.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.ui.NamePairValue;

public class FieldSearchFormInput {

	Element selected;
	String url;
	String searchField;
	String idField;
	protected ObjectTemplate template;
	protected OrderedField field;
	
	public FieldSearchFormInput(ObjectTemplate template, OrderedField field, String url, String searchField, String idField) {
		this.template = template;
		this.field = field;
		this.url = url;
		this.searchField = searchField;
		this.idField = idField;
	}

	public void renderInput(OrderedView panel, Element rootElement, 
			String value, String name,
			boolean nameIsResourceKey) {
		
		rootElement.appendChild(new Element("div").addClass("form-group w-100 fieldSearchInput")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("col-form-label")
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
							.addClass("form-control fieldSearchInput")
							.attr("data-toggle", "dropdown")
							.attr("data-url", url)
							.attr("data-field", searchField)
							.attr("data-id", idField)
							.attr("type", "text")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false")
							.val(name))
					.appendChild(new Element("div")
							.attr("class", "input-group-append")
								.appendChild(new Element("span")
										.attr("class", "input-group-text")
									.appendChild(new Element("i")
											.attr("class", "fas fa-search"))))
					.appendChild(new Element("div")
							.addClass("dropdown-menu")
							.attr("aria-labelledby", String.format("%sDropdown", field.getResourceKey()))))
					.appendChild(new Element("input")
							.attr("id", field.getFormVariable())
							.attr("name", field.getFormVariable())
							.attr("type", "hidden")
							.val(value)
				.appendChild(new Element("small")
						.addClass("col-12")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));
		
	}

}
