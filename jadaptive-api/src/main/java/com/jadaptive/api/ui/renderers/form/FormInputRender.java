package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public abstract class FormInputRender extends FieldInputRender {
	
	public FormInputRender(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public FormInputRender(ObjectTemplate template, String resourceKey, String formVariable, String bundle) {
		super(template, resourceKey, formVariable, bundle);
	}
	
	public final void renderInput(Element rootElement, String value) {
		
		Element myElement;
		rootElement.appendChild(myElement = new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())))
				.appendChild(new Element("input")
						.attr("id", getFormVariable())
						.attr("name", getFormVariableWithParents())
						.addClass("form-control")
						.attr("value", value)
						.attr("autocomplete", "off")
						.attr("type", getInputType()))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.desc", getResourceKey())))));
		
		onRender(myElement, value);

	}

	protected void onRender(Element rootElement, String value) { }
	
	public abstract String getInputType();
}
