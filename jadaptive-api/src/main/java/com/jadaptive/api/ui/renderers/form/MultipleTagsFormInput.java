package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateView;
import com.jadaptive.api.ui.Html;

public class MultipleTagsFormInput {

	private String formVariable;
	private String resourceKey;
	private String bundle;
	
	
	public MultipleTagsFormInput(String resourceKey, String bundle, String formVariable) {
		super();
		this.formVariable = formVariable;
		this.resourceKey = resourceKey;
		this.bundle = bundle;
	}


	public void renderInput(TemplateView panel, Element rootElement, 
			Collection<String> selectedValues) {
		
		Element selected;
		rootElement.appendChild(new Element("div").addClass("mb-3 multipleTagInput jadaptive-tags")
				.appendChild(new Element("label")
						.attr("for", formVariable)
						.addClass("form-label")
						.attr("jad:bundle", bundle)
						.attr("jad:i18n", String.format("%s.name", resourceKey)))
				.appendChild(new Element("div")
						.attr("id", String.format("%sTagsInput", resourceKey))
						.addClass("input-group position-relative")
					.appendChild(new Element("input")
							.attr("id", String.format("%sTags", resourceKey))
							.attr("data-display", "static")
							.attr("data-formvar", formVariable)
							.addClass("form-control multipleTagSource")
							.attr("type", "text"))
					.appendChild(new Element("a")
							.attr("role", "button" )
							.attr("data-formvar", formVariable)
							.attr("class", "input-group-text multipleTagAdd text-decoration-none")
							.appendChild(new Element("i")
									.attr("class", "fa-solid fa-plus"))))
				.appendChild(new Element("div")
						.addClass("row mt-3")
					.appendChild(new Element("div")
							.addClass("col-md-12")
							.appendChild(selected = new Element("ul")
									.attr("name", formVariable)
									.addClass("bg-body form-control w-100 multipleTagTarget jadaptive-select"))))
					.appendChild(new Element("div")
							.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-md-10")
								.appendChild(new Element("small")
										.addClass("text-muted")
										.attr("jad:bundle", bundle)
										.attr("jad:i18n", String.format("%s.desc", resourceKey))))));
		
		for(String value : selectedValues) {
			selected.appendChild(Html.input("hidden", formVariable, value))
					.appendChild(Html.li("badge bg-primary me-1")
							.appendChild(Html.span(value, "pe-1"))
							.appendChild(Html.a("#", "jadaptive-tag")
									.appendChild(Html.i("fa-solid fa-times"))));			
		}
	}

}
