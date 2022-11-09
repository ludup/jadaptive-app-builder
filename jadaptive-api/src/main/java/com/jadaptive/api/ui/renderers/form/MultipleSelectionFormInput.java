package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.template.TemplateView;

public class MultipleSelectionFormInput extends FieldInputMultipleValuesRender {

	Element available;
	Element selected;
	public MultipleSelectionFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	@Override
	public void renderInput(TemplateView panel, Element rootElement, 
			Collection<String> availableValues, 
			Collection<String> selectedValues,
			boolean valueIsResourceKey) {
		
		Element inputs;
		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(inputs = new Element("div")
						.addClass("row")
					.appendChild(new Element("div")
							.attr("id", String.format("%sAvailable", field.getResourceKey()))
							.addClass("col-5")
							.appendChild(available = new Element("select")
									.attr("size", "10")
									.attr("multiple", "mulitple")
									.addClass("w-100 multipleSelectSource")))
					.appendChild(new Element("div")
							.attr("id", String.format("%sButtons", field.getResourceKey()))
							.addClass("col-1")
							.appendChild(new Element("a")
									.attr("href", "#")
									.addClass("btn btn-primary mt-3 multipleSelectAdd")
									.appendChild(new Element("i")
											.addClass("far fa-chevron-right")))
							.appendChild(new Element("a")
									.attr("href", "#")
									.addClass("btn btn-primary mt-3 multipleSelectRemove")
									.appendChild(new Element("i")
											.addClass("far fa-chevron-left"))))
					.appendChild(new Element("div")
							.attr("id", String.format("%sSelected", field.getResourceKey()))
							.addClass("col-5")
							.appendChild(selected = new Element("select")
										.attr("id", field.getFormVariable())
										.attr("size", "10")
										.attr("multiple", "mulitple")
										.addClass("w-100 multipleSelectTarget jadaptive-select"))))));

		
		for(String value : availableValues) {
			if(!selectedValues.contains(value)) {
				Element displayName;
				available.appendChild(new Element("option")
								.val(value)
								.appendChild(displayName = new Element("span").text(value)));
								
				if(valueIsResourceKey) {
					displayName.attr("jad:bundle", field.getBundle())
								.attr("jad:i18n", value);
				}
			}
		}
		
		for(String value : selectedValues) {
			Element displayName;
			selected.appendChild(new Element("option")
							.val(value)
							.appendChild(displayName = new Element("span").text(value)));
			inputs.appendChild(Html.input("text", field.getFormVariable(), value).addClass("d-none"));		
			if(valueIsResourceKey) {
				displayName.attr("jad:bundle", field.getBundle())
							.attr("jad:i18n", value);
			}
		}
	}

}
