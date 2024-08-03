package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;
import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.TemplateViewField;

public class MultipleAttachmentInput extends FieldInputRender {

	public MultipleAttachmentInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	public MultipleAttachmentInput(TemplateViewField field) {
		super(field);
	}



	@Override
	public void renderInput(Element rootElement, String value, String... classes) throws IOException {
		
		UploadFormInput input = new UploadFormInput(resourceKey, formVariable, bundle);
		input.renderInput(rootElement, value);
	}

	public void renderAttachments(Collection<AbstractObject> collection) {
		
		
	}

	

}
