package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

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
			boolean nameIsResourceKey,
			boolean readOnly) {
		
		rootElement.appendChild(new Element("div")
				.addClass("row mb-3 fieldSearchInput")
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
							.addClass("form-control jsearchText")
							.attr("data-bs-toggle", "dropdown")
							.attr("data-url", url)
							.attr("data-field", searchField)
							.attr("data-id", idField)
							.attr("type", "text")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false")
							.attr("readOnly", readOnly)
							.val(name))
					.appendChild(new Element("input")
							.attr("id", field.getFormVariable())
							.attr("name", field.getFormVariable())
							.attr("type", "hidden")
							.val(value))
					.appendChild(new Element("span")
							.attr("class", "input-group-text")
							.appendChild(new Element("i")
								.attr("class", "fas fa-search")))
					.appendChild(new Element("div")
							.addClass(readOnly ? "disabled-dropdown" : "dropdown-menu")
							.attr("aria-labelledby", String.format("%sDropdown", field.getResourceKey()))))
				.appendChild(new Element("small")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));
		
	}

}
