package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class FieldSearchFormInput {

	Element selected;
	String url;
	String searchField;
	String idField;
	protected ObjectTemplate template;
	
	String resourceKey;
	String formVariable;
	String bundle;
	
	public FieldSearchFormInput(ObjectTemplate template, TemplateViewField field, String url, String searchField, String idField) {
		this.template = template;
		this.url = url;
		this.searchField = searchField;
		this.idField = idField;
		this.resourceKey = field.getResourceKey();
		this.formVariable = field.getFormVariable();
		this.bundle = field.getBundle();
	}
	
	public FieldSearchFormInput(ObjectTemplate template, String resourceKey, String formVariable, String bundle, String url, String searchField, String idField) {
		this.template = template;
		this.url = url;
		this.searchField = searchField;
		this.idField = idField;
		this.resourceKey = resourceKey;
		this.formVariable = formVariable;
		this.bundle = bundle;
	}

	public void renderInput(Element rootElement, 
			String value, String name,
			boolean nameIsResourceKey,
			boolean readOnly) {
		renderInput(rootElement, bundle, resourceKey, formVariable,
				value, name, nameIsResourceKey, readOnly);
	}
	
	public void renderInput(Element rootElement, 
			String bundle,
			String resourceKey,
			String variableName,
			String value, 
			String name,
			boolean nameIsResourceKey,
			boolean readOnly) {
		rootElement.appendChild(new Element("div")
				.addClass("row mb-3 fieldSearchInput")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", variableName)
						.addClass("form-label")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey)))
				.appendChild(new Element("div")
						.attr("id", String.format("%sDropdown", resourceKey))
						.addClass("input-group position-relative dropdown")
					.appendChild(new Element("input")
							.attr("id", String.format("%sText", resourceKey))
							.attr("name", String.format("%sText", variableName))
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
							.attr("id", variableName)
							.attr("name", variableName)
							.attr("type", "hidden")
							.val(value))
					.appendChild(new Element("span")
							.attr("class", "input-group-text")
							.appendChild(new Element("i")
								.attr("class", "fa-solid fa-search")))
					.appendChild(new Element("div")
							.addClass(readOnly ? "disabled-dropdown" : "dropdown-menu dropdown-size")
							.attr("aria-labelledby", String.format("%sDropdown", resourceKey))))
				.appendChild(new Element("small")
						.addClass("text-muted")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.desc", resourceKey)))));
		
	}

}
