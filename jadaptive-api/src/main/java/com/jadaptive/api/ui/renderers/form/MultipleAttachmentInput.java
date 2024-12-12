package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;
import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.files.FileAttachment;
import com.jadaptive.api.files.FileAttachmentService;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.utils.Utils;

public class MultipleAttachmentInput extends FieldInputRender {

	UploadFormInput input;
	
	public MultipleAttachmentInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	public MultipleAttachmentInput(TemplateViewField field) {
		super(field);
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) throws IOException {
		
		input = new UploadFormInput(resourceKey, formVariable, bundle);
		input.renderInput(rootElement, value);
	}

	public void renderAttachments(Collection<AbstractObject> objects) {
		
		for(AbstractObject obj : objects) {
			FileAttachmentService s = ApplicationServiceImpl.getInstance().getBean(FileAttachmentService.class);
			FileAttachment file = s.getAttachment(obj.getUuid());
			input.renderExistingFile(file.getUuid(),
				file.getFilename(),
				Utils.toByteSize(file.getSize(), 0));
		}
	}

	

}
