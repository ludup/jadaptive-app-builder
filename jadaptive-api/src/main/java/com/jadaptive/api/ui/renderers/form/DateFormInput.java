package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class DateFormInput extends FormInputRender {

	public DateFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public DateFormInput(ObjectTemplate template, String resourceKey, String formVariable, String bundle) {
		super(template, resourceKey, formVariable, bundle);
	}
	
	public DateFormInput(String resourceKey,String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}
	
	
	@Override
	public String getInputType() {
		return "date";
	}
}
