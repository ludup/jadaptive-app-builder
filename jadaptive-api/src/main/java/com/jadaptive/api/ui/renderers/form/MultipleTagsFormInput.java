package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.TemplateView;
import com.jadaptive.api.ui.Html;

public class MultipleTagsFormInput {

	Element selected;
	protected ObjectTemplate template;
	protected TemplateViewField field;
	
//	public MultipleTagsFormInput(ObjectTemplate template, OrderedField field) {
//		this.template = template;
//		this.field = field;
//	}

	public void renderInput(TemplateView panel, Element rootElement, 
			Collection<String> selectedValues) {
		
		rootElement.appendChild(new Element("div").addClass("row mb-3 multipleTagInput")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("div")
						.attr("id", String.format("%sTagsInput", field.getResourceKey()))
						.addClass("input-group position-relative")
					.appendChild(new Element("input")
							.attr("id", String.format("%sTags", field.getResourceKey()))
							.attr("data-display", "static")
							.addClass("form-control multipleTagSource")
							.attr("type", "text"))
					.appendChild(new Element("a")
							.attr("role", "button" )
							.attr("class", "input-group-text multipleTagAdd text-decoration-none")
							.appendChild(new Element("i")
									.attr("class", "fa-solid fa-plus"))))
				.appendChild(new Element("div")
						.addClass("row mt-3")
					.appendChild(new Element("div")
							.addClass("col-md-12")
							.appendChild(selected = new Element("select")
									.attr("id", field.getFormVariable())
									.attr("size", "2")
									.attr("name", field.getFormVariable())
									.attr("multiple", "mulitple")
									.addClass("bg-body form-control w-100 multipleTagTarget jadaptive-select"))))
					.appendChild(new Element("div")
							.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-md-10")
								.appendChild(new Element("small")
										.addClass("text-muted")
										.attr("jad:bundle", field.getBundle())
										.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))))));
		
		for(String value : selectedValues) {
			selected.appendChild(Html.option(value, "badge bg-primary me-1")
							.appendChild(Html.span(value, "pe-1"))
							.appendChild(Html.a("#", "jadaptive-tag")
									.appendChild(Html.i("fa-solid fa-times me-3"))));			
		}
	}

}
