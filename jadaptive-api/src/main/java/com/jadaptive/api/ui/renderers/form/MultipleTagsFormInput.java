package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.ui.Html;

public class MultipleTagsFormInput {

	Element selected;
	protected ObjectTemplate template;
	protected OrderedField field;
	
//	public MultipleTagsFormInput(ObjectTemplate template, OrderedField field) {
//		this.template = template;
//		this.field = field;
//	}

	public void renderInput(OrderedView panel, Element rootElement, 
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
						.attr("style", "position: relative")
						.addClass("input-group")
					.appendChild(new Element("input")
							.attr("id", String.format("%sTags", field.getResourceKey()))
							.attr("data-display", "static")
							.addClass("form-control multipleTagSource")
							.attr("type", "text"))
					.appendChild(new Element("a")
							.attr("style", "text-decoration: none; cursor: pointer;" )
							.attr("class", "input-group-text multipleTagAdd")
							.appendChild(new Element("i")
									.attr("class", "fas fa-plus"))))
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
									.appendChild(Html.i("far fa-times me-3"))));			
		}
	}

}
