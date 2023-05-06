package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.TemplateView;

public class MultipleTextFormInput {

	Element selected;
	protected ObjectTemplate template;
	protected TemplateViewField field;
	
//	public MultipleTextFormInput(ObjectTemplate template, OrderedField field) {
//		this.template = template;
//		this.field = field;
//	}

	public void renderInput(TemplateView panel, Element rootElement, 
			Collection<String> selectedValues, boolean readOnly) {
		
		rootElement.appendChild(new Element("div").addClass("row mb-3 multipleTextInput")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("div")
						.attr("id", String.format("%sTextInput", field.getResourceKey()))
						.addClass("input-group position-relative")
					.appendChild(new Element("input")
							.attr("id", String.format("%sText", field.getResourceKey()))
							.attr("data-display", "static")
							.addClass("form-control multipleTextSource")
							.attr("type", "text"))
					.appendChild(new Element("a")
							.attr("role", "button" )
							.attr("class", "input-group-text multipleTextAdd text-decoration-none")
							.appendChild(new Element("i")
									.attr("class", "fa-solid fa-plus"))))
				.appendChild(new Element("div")
						.addClass("row mt-3")
					.appendChild(new Element("div")
							.addClass("col-md-12")
							.appendChild(selected = new Element("select")
									.attr("id", field.getFormVariable())
									.attr("size", "5")
									.attr("name", field.getFormVariable())
									.attr("multiple", "mulitple")
									.addClass("form-control w-100 multipleTextTarget jadaptive-select"))))
					.appendChild(new Element("div")
							.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-md-10")
								.appendChild(new Element("small")
										.addClass("text-muted")
										.attr("jad:bundle", field.getBundle())
										.attr("jad:i18n", String.format("%s.desc", field.getResourceKey()))))
						.appendChild(new Element("div")
								.addClass("col-md-2 text-end mt-1")
								.appendChild(new Element("button")
									.addClass("btn btn-danger btn-sm multipleTextRemove")
									.attr("href", "#")
									.appendChild(new Element("i")
											.addClass("fa-solid fa-trash"))
//									.appendChild(new Element("span")
//											.addClass("ms-1")
//											.attr("jad:bundle", "default")
//											.attr("jad:i18n", "delete.name"))
											)		
								))));
		
		for(String value : selectedValues) {
			selected.appendChild(new Element("option")
							.val(value)
							.appendChild(new Element("span").text(value)));			
		}
	}

}
