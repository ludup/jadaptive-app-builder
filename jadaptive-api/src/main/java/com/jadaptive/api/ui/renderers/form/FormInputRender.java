package com.jadaptive.api.ui.renderers.form;

import java.util.Objects;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public abstract class FormInputRender extends FieldInputRender {
	
	public FormInputRender(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public final void renderInput(Element rootElement, String value) {
		
		StringBuffer formVariable = new StringBuffer();
		
		if(Objects.nonNull(field.getParentFields())) {
			for(FieldTemplate t : field.getParentFields()) {
				formVariable.append(t.getResourceKey());
				formVariable.append(".");
			}
		}
		
		formVariable.append(field.getFormVariable());
		
		Element myElement;
		rootElement.appendChild(myElement = new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("input")
						.attr("id", field.getFormVariable())
						.attr("name", formVariable.toString())
						.addClass("form-control")
						.attr("value", value)
						.attr("autocomplete", "off")
						.attr("type", getInputType()))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));
		
		onRender(myElement, value);

	}

	protected void onRender(Element rootElement, String value) { }
	
	public abstract String getInputType();
}
