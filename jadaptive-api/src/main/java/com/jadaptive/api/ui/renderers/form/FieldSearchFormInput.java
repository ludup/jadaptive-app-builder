package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class FieldSearchFormInput {

	Element selected;
	String url;
	String searchField;
	String idField;
	String valueField;
	protected ObjectTemplate template;
	
	String resourceKey;
	String formVariable;
	String bundle;
	boolean decorate = true;
	boolean disableIDAttribute; 
	
	public FieldSearchFormInput(ObjectTemplate template, TemplateViewField field, String url, String searchField, String idField) {
		this.template = template;
		this.url = url;
		this.searchField = searchField;
		this.idField = idField;
		this.valueField = idField;
		this.resourceKey = field.getResourceKey();
		this.formVariable = field.getFormVariable();
		this.bundle = field.getBundle();
	}
	
	public FieldSearchFormInput(ObjectTemplate template, String resourceKey, String formVariable, String bundle, String url, String searchField, String idField, String valueField) {
		this.template = template;
		this.url = url;
		this.searchField = searchField;
		this.idField = idField;
		this.valueField = valueField;
		this.resourceKey = resourceKey;
		this.formVariable = formVariable;
		this.bundle = bundle;
	}

	public Element renderInput(Element rootElement, 
			String value, String name,
			boolean nameIsResourceKey,
			boolean readOnly) {
		return renderInput(rootElement, bundle, resourceKey, formVariable,
				value, name, nameIsResourceKey, readOnly);
	}
	
	public Element renderInput(Element rootElement, 
			String bundle,
			String resourceKey,
			String variableName,
			String value, 
			String name,
			boolean nameIsResourceKey,
			boolean readOnly) {
		Element _this;
		rootElement.appendChild(_this = new Element("div")
				.addClass("row mb-3 fieldSearchInput"));
		
		if(decorate) {
			_this.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", variableName)
						.addClass("form-label")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey))));
		}
		
		Element input;
		Element inputText;
		_this.appendChild(new Element("div")
						.attr("id", String.format("%sDropdown", resourceKey))
						.addClass("input-group position-relative dropdown")
					.appendChild(inputText = new Element("input")
							.attr("autocomplete", "off")
							.attr("name", String.format("%sText", variableName))
							.attr("data-display", "static")
							.addClass(String.format("%sText", resourceKey) + " form-control jsearchText")
							.attr("data-bs-toggle", "dropdown")
							.attr("data-url", url)
							.attr("data-field", searchField)
							.attr("data-id", valueField)
							.attr("type", "text")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false")
							.attr("readOnly", readOnly)
							.val(name))
					.appendChild(input = new Element("input")
							.attr("name", variableName)
							.addClass(idField)
							.attr("type", "hidden")
							.val(value))
					.appendChild(new Element("span")
							.attr("class", "input-group-text")
							.appendChild(new Element("i")
								.attr("class", "fa-solid fa-search")))
					.appendChild(new Element("div")
							.addClass(readOnly ? "disabled-dropdown" : "dropdown-menu dropdown-size")
							.attr("aria-labelledby", String.format("%sDropdown", resourceKey))));
		
		if(decorate) {
				_this.appendChild(new Element("small")
						.addClass("text-muted")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.desc", resourceKey)));
		}
		
		if(!disableIDAttribute) {
			inputText.attr("id", String.format("%sText", resourceKey));
			input.attr("id", idField);
			
		}
		
		return _this;
		
	}
	
	public void diableDecoration() {
		this.decorate = false;
	}

	public void disableIDAttribute() {
		this.disableIDAttribute = true;
	}

}
