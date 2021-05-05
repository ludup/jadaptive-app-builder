package com.jadaptive.app.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.ui.NamePairValue;

public class MultipleSearchFormInput {

	Element selected;
	String url;
	String searchField;
	String idField;
	protected ObjectTemplate template;
	protected OrderedField field;
	
	public MultipleSearchFormInput(ObjectTemplate template, OrderedField field, String url, String searchField, String idField) {
		this.template = template;
		this.field = field;
		this.url = url;
		this.searchField = searchField;
		this.idField = idField;
	}

	public void renderInput(OrderedView panel, Element rootElement, 
			Collection<NamePairValue> selectedValues,
			boolean nameIsResourceKey) {
		
		rootElement.appendChild(new Element("div").addClass("form-group w-100 multipleSearchInput")
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
							.addClass("form-control multipleSearchInputText")
							.attr("data-toggle", "dropdown")
							.attr("data-url", url)
							.attr("data-field", searchField)
							.attr("data-id", idField)
							.attr("type", "text")
							.attr("aria-haspopup", "true")
							.attr("aria-expanded", "false"))
					.appendChild(new Element("div")
							.attr("class", "input-group-append")
								.appendChild(new Element("span")
										.attr("class", "input-group-text")
									.appendChild(new Element("i")
											.attr("class", "fas fa-search"))))
					.appendChild(new Element("div")
							.addClass("dropdown-menu")
							.attr("aria-labelledby", String.format("%sDropdown", field.getResourceKey()))))
				.appendChild(new Element("div")
						.addClass("row mt-3")
					.appendChild(new Element("div")
							.attr("id", field.getFormVariable())
							.addClass("col-md-12")
							.appendChild(selected = new Element("select")
									.attr("size", "10")
									.attr("name", field.getFormVariable())
									.attr("multiple", "mulitple")
									.addClass("w-100 multipleSearchTarget"))))
					.appendChild(new Element("div")
							.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-md-10")
								.appendChild(new Element("small")
										.attr("jad:bundle", field.getBundle())
										.attr("jad:i18n", String.format("%s.desc", field.getResourceKey()))))
						.appendChild(new Element("div")
								.addClass("col-md-2 text-right mt-1")
								.appendChild(new Element("button")
									.addClass("btn btn-danger btn-sm multipleSearchDelete")
									.attr("href", "#")
									.appendChild(new Element("i")
											.addClass("far fa-trash"))
									.appendChild(new Element("span")
											.addClass("ml-1")
											.attr("jad:bundle", "default")
											.attr("jad:i18n", "delete.name")))	
//								.appendChild(new Element("button")
//										.addClass("btn btn-primary btn-sm multipleSearchCreate")
//										.attr("href", "/app/ui/create/" + template.getResourceKey())
//										.appendChild(new Element("i")
//												.addClass("far fa-plus"))
//										.appendChild(new Element("span")
//												.addClass("ml-1")
//												.attr("jad:bundle", "default")
//												.attr("jad:i18n", "create.name")))	
								)));
		for(NamePairValue value : selectedValues) {
			Element displayName;
			selected.appendChild(new Element("option")
							.val(value.getValue())
							.appendChild(displayName = new Element("span").text(value.getName())));			
			if(nameIsResourceKey) {
				displayName.attr("jad:bundle", field.getBundle())
							.attr("jad:i18n", value.getName());
			}
		}
	}

}
