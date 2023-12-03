package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.TemplateView;

public class FileFormInput extends FormInputRender {

	public FileFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	@Override
	public String getInputType() {
		return "file";
	}
	
	protected void onRender(TemplateView panel, Element rootElement, String value) { 
		
		rootElement.selectFirst("input").val("");
		rootElement.appendChild(new Element("input")
							.attr("name", getFormVariable() + "_previous")
							.attr("type", "hidden")
							.val(value));
	}

}
