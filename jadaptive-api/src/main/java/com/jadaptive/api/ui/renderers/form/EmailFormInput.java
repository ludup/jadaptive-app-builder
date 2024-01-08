package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class EmailFormInput extends TextFormInput {

	public EmailFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	public EmailFormInput(ObjectTemplate template, String resourceKey, String formVariable, String bundle) {
		super(template, resourceKey, formVariable, bundle);
	}

	@Override
	public String getInputType() {
		return "email";
	}

	
}
