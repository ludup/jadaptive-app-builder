package com.jadaptive.app.ui.renderers.form;

import java.util.Objects;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationType;

public class SearchFormInput extends FieldInputRender {

	public SearchFormInput(ObjectTemplate template, FieldTemplate field) {
		super(template, field);
	}

	@Override
	public void renderInput(Elements rootElement, String defaultValue) {

		String url = "";
		if(field.getFieldType()==FieldType.OBJECT_REFERENCE) {
			url = String.format("/app/api/%s/table", field.getValidationValue(ValidationType.RESOURCE_KEY));
		} else {
			try {
				url = field.getValidationValue(ValidationType.URL);
			} catch(Throwable e) { }
		}
		rootElement.append(replaceResourceKey("<div class=\"form-group col-12\"><label for=\"${resourceKey}\" class=\"col-form-label\" ${i18nName}></label><div id=\"${resourceKey}Dropdown\" style=\"position: relative\" class=\"input-group dropdown\"></div></div>"));
		Elements dropdown = rootElement.select(replaceResourceKey("#${resourceKey}Dropdown"));
		dropdown.append(replaceResourceKey("<input id=\"${resourceKey}\" name=\"${resourceKey}\" type=\"hidden\">"));
		dropdown.append(replaceResourceKey("<input id=\"${resourceKey}Text\" data-display=\"static\" data-searchurl=\"" + url + "\" class=\"jsearchText form-control\" type=\"text\" aria-haspopup=\"true\" aria-expanded=\"false\">"));
		dropdown.append(replaceResourceKey("<div class=\"input-group-append\"><span class=\"jsearchClick input-group-text\"><i class=\"fas fa-search\"></i></span></div>"));
		dropdown.append(replaceResourceKey("<div class=\"dropdown-menu\" aria-labelledby=\"${resourceKey}Dropdown\"></div>"));
		dropdown.parents().first().append("<small class=\"form-text text-muted\" ${i18nDesc}></small>");
		@SuppressWarnings("unused")
		Element name = dropdown.select(replaceResourceKey("#${resourceKey}Text")).first();
		@SuppressWarnings("unused")
		Element value = dropdown.select(replaceResourceKey("#${resourceKey}")).first();
		
		if(Objects.nonNull(defaultValue)) {
			// TODO set value from reference
		} 
	}

}
