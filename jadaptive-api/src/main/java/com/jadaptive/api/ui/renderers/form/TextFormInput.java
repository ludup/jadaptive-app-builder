package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class TextFormInput extends FormInputRender {

	public TextFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	public TextFormInput(ObjectTemplate template, String resourceKey, String formVariable, String bundle) {
		super(template, resourceKey, formVariable, bundle);
	}
	
	public TextFormInput(String resourceKey,String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}


	@Override
	public String getInputType() {
		return "text";
	}

}
