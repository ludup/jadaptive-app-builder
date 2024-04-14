package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class PasswordFormInput extends FormInputRender {

	public PasswordFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public PasswordFormInput(String resourceKey,String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	@Override
	public String getInputType() {
		return "password";
	}

	
}
