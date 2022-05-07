package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public class ImageFormInput extends FormInputRender {

	public ImageFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}

	@Override
	public String getInputType() {
		return "file";
	}
	
	protected void onRender(OrderedView panel, Element rootElement, String value) { 
		
		rootElement.selectFirst("small").after(
				new Element("div").addClass("col-12 my-3").appendChild(
						new Element("img").attr("src", value)));
		
		rootElement.selectFirst("input").val("");
		rootElement.appendChild(new Element("input")
							.attr("name", field.getFormVariable() + "_previous")
							.attr("type", "hidden")
							.val(value));
	}

}
