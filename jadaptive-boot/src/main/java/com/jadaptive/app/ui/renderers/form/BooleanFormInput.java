package com.jadaptive.app.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;

public class BooleanFormInput extends FieldInputRender {

	public BooleanFormInput(ObjectTemplate template, FieldTemplate field) {
		super(template, field);
	}

	@Override
	public void renderInput(Element rootElement, String value) {
		rootElement.append(replaceResourceKey("<div id=\"${resourceKey}Group\" class=\"form-check col-12\"></div>")); 
		Element div = rootElement.select(replaceResourceKey("#${resourceKey}Group")).first();
		div.append(replaceResourceKey("<div><label for=\"${resourceKey}\" class=\"col-form-label\" ${i18nName}></label></div>"));
		div.append(replaceResourceKey("<input type=\"checkbox\" data-toggle=\"toggle\" id=\"${resourceKey}\" name=\"${resourceKey}\" class=\"form-check-input\" value=\"" + value + "\">"));
		div.append(replaceResourceKey("<small class=\"form-text text-muted\" ${i18nDesc}></small>"));

		if("true".equalsIgnoreCase(value)) {
			div.select(replaceResourceKey("#${resourceKey}Bool")).attr("checked", "checked");
		}
	}

}
