package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class TimeFormInput extends FormInputRender {

	public TimeFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	@Override
	public String getInputType() {
		return "time";
	}
}
