package com.jadaptive.app.ui.renderers.form;

import java.util.Objects;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ValidationType;

public class SearchFormInput extends FormInputRender {

	FieldTemplate template;
	public SearchFormInput(Elements rootElement, FieldTemplate template, String defaultValue) {
		super(rootElement, template, defaultValue);
	}

	@Override
	protected void renderInput(Elements rootElement, FieldTemplate template, String defaultValue) {

		String url = "";
		if(template.getFieldType()==FieldType.OBJECT_REFERENCE) {
			url = String.format("/app/api/%s/table", template.getValidationValue(ValidationType.RESOURCE_KEY));
		} else {
			try {
				url = template.getValidationValue(ValidationType.URL);
			} catch(Throwable e) { }
		}
		rootElement.append(replaceResourceKey("<div class=\"form-group col-12\"><label for=\"${resourceKey}\" class=\"col-form-label\">" + template.getName() + "</label><div id=\"${resourceKey}Dropdown\" style=\"position: relative\" class=\"input-group dropdown\"></div></div>"));
		Elements dropdown = rootElement.select(replaceResourceKey("#${resourceKey}Dropdown"));
		dropdown.append(replaceResourceKey("<input id=\"${resourceKey}\" name=\"${resourceKey}\" type=\"hidden\">"));
		dropdown.append(replaceResourceKey("<input id=\"${resourceKey}Text\" data-display=\"static\" data-searchurl=\"" + url + "\" class=\"jsearchText form-control\" type=\"text\" aria-haspopup=\"true\" aria-expanded=\"false\">"));
		dropdown.append(replaceResourceKey("<div class=\"input-group-append\"><span class=\"jsearchClick input-group-text\"><i class=\"fas fa-search\"></i></span></div>"));
		dropdown.append(replaceResourceKey("<div class=\"dropdown-menu\" aria-labelledby=\"${resourceKey}Dropdown\"></div>"));
		dropdown.parents().first().append("<small class=\"form-text text-muted\">" + template.getDescription() + "</small>");
		Element name = dropdown.select(replaceResourceKey("#${resourceKey}Text")).first();
		Element value = dropdown.select(replaceResourceKey("#${resourceKey}")).first();
		
		if(Objects.nonNull(defaultValue)) {
			// TODO set value from reference
		} 
	}

}
