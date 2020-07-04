package com.jadaptive.app.ui.renderers.form;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;

public class TextAreaFormInput extends FieldInputRender {

	public TextAreaFormInput(ObjectTemplate template, FieldTemplate field) {
		super(template, field);
	}

	@Override
	public void renderInput(Elements rootElement, String value) {

		rootElement.append(replaceResourceKey("<div id=\"${resourceKey}Group\" class=\"form-group col-12\"></div>")); 
		Element div = rootElement.select(replaceResourceKey("#${resourceKey}Group")).first();
		div.append(replaceResourceKey("<label for=\"${resourceKey}\" class=\"col-form-label\"  ${i18nName}></label>"));
		div.append(replaceResourceKey("<textarea id=\"${resourceKey}\" name=\"${resourceKey}\" class=\"form-control\">" + value + "</textarea>"));
		div.append(replaceResourceKey("<small class=\"form-text text-muted\" ${i18nDesc}></small>"));

	}

}
