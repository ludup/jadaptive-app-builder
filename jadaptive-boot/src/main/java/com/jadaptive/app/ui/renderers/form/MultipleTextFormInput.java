package com.jadaptive.app.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public class MultipleTextFormInput {

	Element selected;
	protected ObjectTemplate template;
	protected OrderedField field;
	
	public MultipleTextFormInput(ObjectTemplate template, OrderedField field) {
		this.template = template;
		this.field = field;
	}

	public void renderInput(OrderedView panel, Element rootElement, 
			Collection<String> selectedValues) {
		
		rootElement.appendChild(new Element("div").addClass("form-group w-100 multipleTextInput")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("col-form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("div")
						.attr("id", String.format("%sTextInput", field.getResourceKey()))
						.attr("style", "position: relative")
						.addClass("input-group")
					.appendChild(new Element("input")
							.attr("id", String.format("%sText", field.getResourceKey()))
							.attr("data-display", "static")
							.addClass("form-control multipleTextInputText")
							.attr("type", "text"))
					.appendChild(new Element("div")
							.attr("class", "input-group-append")
								.appendChild(new Element("span")
										.attr("class", "input-group-text")
									.appendChild(new Element("i")
											.attr("class", "fas fa-plus"))))
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
									.addClass("btn btn-primary btn-sm multipleTextDelete")
									.attr("href", "#")
									.appendChild(new Element("i")
											.addClass("far fa-trash"))
									.appendChild(new Element("span")
											.addClass("ml-1")
											.attr("jad:bundle", "default")
											.attr("jad:i18n", "delete.name")))		
								)));
		
		for(String value : selectedValues) {
			selected.appendChild(new Element("option")
							.val(value)
							.appendChild(new Element("span").text(value)));			
		}
	}

}
