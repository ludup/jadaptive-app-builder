package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.TemplateView;
import com.jadaptive.api.ui.Html;

public class CollectionTextFormInput {

	Element table;
	protected ObjectTemplate template;
	protected TemplateViewField field;
	Element input;
	
	public CollectionTextFormInput(ObjectTemplate template, TemplateViewField field) {
		this.template = template;
		this.field = field;
	}

	public void renderInput(TemplateView panel, Element rootElement, 
			Collection<String> selectedValues, boolean readOnly) {

		Element div;
		rootElement.appendChild(div = new Element("div").addClass("row collectionTextInput")
				.attr("data-resourcekey", field.getResourceKey())
				.appendChild(input = new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))));
		
		if(!readOnly) {
				input.appendChild(new Element("div")
						.attr("id", String.format("%sDropdown", field.getResourceKey()))
						.addClass("input-group position-relative dropdown mb-3")
					.appendChild(input = new Element("input")
							.attr("id", String.format("%sText", field.getResourceKey()))
							.addClass("form-control collectionTextInputText")
							.attr("autocomplete", "off")
							.attr("type", "text"))
					.appendChild(new Element("span")
							.addClass("input-group-text collectionTextAdd")
						.appendChild(new Element("i")
								.attr("class", "fa-solid fa-plus")))
					.appendChild(new Element("div")
							.addClass("dropdown-menu dropdown-size")
							.attr("aria-labelledby", String.format("%sDropdown", field.getResourceKey()))));
		}
		
		Element tr;
		
		div.appendChild(new Element("div")
						.addClass("row")
					.appendChild(new Element("div")
							.attr("id", field.getFormVariable())
							.addClass("col-md-12")
							.appendChild(new Element("table")
									.addClass("w-100 collectionSearchTarget table table-sm table-striped")
								.appendChild(new Element("thead")
										.appendChild(tr = new Element("tr")
												.appendChild(new Element("td")
														.attr("jad:bundle","default")
														.attr("jad:i18n", "name.name")))
										.appendChild(table = new Element("tbody"))))
							))
					.appendChild(new Element("div")
							.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-md-10")
								.appendChild(new Element("small")
										.addClass("text-muted")
										.attr("jad:bundle", field.getBundle())
										.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));
		if(!readOnly) {
			tr.appendChild(new Element("td")
					.attr("jad:bundle","default")
					.attr("jad:i18n", "actions.name"));
		}
		
		for(String value : selectedValues) {
			
			table.appendChild(tr = new Element("tr")
					.appendChild(new Element("input")
							.attr("type", "hidden")
							.attr("name", field.getResourceKey())
							.attr("value", value))
					.appendChild(new Element("td")
							.appendChild(Html.span(value, "underline"))));
			
			if(!readOnly) {
					tr.appendChild(new Element("td")
							.appendChild(Html.a("#", "collectionSearchDelete")
									.appendChild(Html.i("fa-solid", "fa-fw", "fa-trash", "me-2"))));		
			}
		}
	}
	
	public Element getInputElement() {
		return input;
	}

}
