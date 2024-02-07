package com.jadaptive.api.ui.renderers.form;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.ValidationType;

public class ImageFormInput extends FormInputRender {

	private String classes = "col-12 my-3";
	private boolean resettable;
	
	public ImageFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
		resettable = field.getField().isResettable();
		try {
			classes += " " + field.getField().getValidationValue(ValidationType.CLASSES);
		} catch(UnsupportedOperationException e) { }
	}

	@Override
	public String getInputType() {
		return "file";
	}
	
	@Override
	protected void onRender(Element rootElement, String value) {  

		rootElement.selectFirst("small").after(
				new Element("div").addClass(classes)
								.appendChild(
						new Element("img").
							attr("id", getFormVariable() + "_preview").
							attr("src", value)));
		
		Element inputEl = rootElement.selectFirst("input");
		inputEl.val("");
		if(resettable && StringUtils.isNotBlank(value)) {
			inputEl.after(new Element("div").addClass("input-group-text").appendChild(
				new Element("a").
				  attr("href", "#").
				  addClass("input-resetter").
				  attr("data-reset-type", "image").
				  attr("data-reset-for", getFormVariable()).
				  appendChild(
					new Element("i").addClass("fa fa-broom-wide")
				)
			));
		}
		rootElement.appendChild(new Element("input")
							.attr("name", getFormVariable() + "_previous")
							.attr("type", "hidden")
							.val(value));
	}

}
