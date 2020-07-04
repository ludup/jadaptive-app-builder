package com.jadaptive.app.ui.renderers.form;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;

public abstract class FormInputRender extends FieldInputRender {
	
	public FormInputRender(ObjectTemplate template, FieldTemplate field) {
		super(template, field);
	}
	
	public final void renderInput(Elements rootElement, String value) {
		rootElement.append(replaceResourceKey("<div id=\"${resourceKey}Group\" class=\"form-group col-12\"></div>")); 
		Element div = rootElement.select(replaceResourceKey("#${resourceKey}Group")).first();
		div.append(replaceResourceKey("<label for=\"${resourceKey}\" class=\"col-form-label\" ${i18nName}></label>"));
		div.append(replaceResourceKey("<input type=\"${inputType}\" id=\"${resourceKey}\" name=\"${resourceKey}\" class=\"form-control\" value=\"" + value + "\">"));
		div.append(replaceResourceKey("<small class=\"form-text text-muted\" ${i18nDesc}></small>"));

	}

	public abstract String getInputType();
	
	protected String replaceResourceKey(String str) {
		return super.replaceResourceKey(str).replace("${inputType}", getInputType());
	}
}
