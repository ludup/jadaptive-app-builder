package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;

public class UploadFormInput extends FieldInputRender {

	public UploadFormInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	public UploadFormInput(TemplateViewField field) {
		super(field);
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) throws IOException {
		
		load(rootElement);
		
	}


	

}
