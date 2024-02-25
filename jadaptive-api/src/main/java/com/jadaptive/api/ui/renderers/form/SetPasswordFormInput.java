package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class SetPasswordFormInput extends PasswordFormInput {

	public SetPasswordFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	@Override
	protected String getAutocomplete() {
		return "new-password";
	}

	
}
