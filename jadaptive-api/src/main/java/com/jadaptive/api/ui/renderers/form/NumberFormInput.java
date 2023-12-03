package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class NumberFormInput extends FormInputRender {

	public NumberFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public NumberFormInput(ObjectTemplate template, String resourceKey, String formVariable, String bundle) {
		super(template, resourceKey, formVariable, bundle);
	}

	@Override
	public String getInputType() {
		return "number";
	}

		
	
	
}
