package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.ValidationType;

public class ImageFormInput extends FormInputRender {

	public ImageFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	@Override
	public String getInputType() {
		return "file";
	}
	
	@Override
	protected void onRender(Element rootElement, String value) {  
		String classes = "col-12 my-3";
		try {
			classes += " " + field.getField().getValidationValue(ValidationType.CLASSES);
		} catch(UnsupportedOperationException e) { }
		
		rootElement.selectFirst("small").after(
				new Element("div").addClass(classes)
								.appendChild(
						new Element("img").attr("src", value)));
		
		rootElement.selectFirst("input").val("");
		rootElement.appendChild(new Element("input")
							.attr("name", field.getFormVariable() + "_previous")
							.attr("type", "hidden")
							.val(value));
	}

}
