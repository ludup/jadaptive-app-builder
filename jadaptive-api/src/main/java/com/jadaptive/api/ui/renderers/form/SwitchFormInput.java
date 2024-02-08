package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;

public class SwitchFormInput extends BooleanFormInput {

	public SwitchFormInput(String resourceKey, String formVariable, String bundle) {
		super(resourceKey, formVariable, bundle);
	}

	public SwitchFormInput(TemplateViewField field) {
		super(field);
	}

	@Override
	protected Element createContainer() {
		var el = createContainerElement();
		el.addClass("form-switch");
		return el;
	}

	@Override
	protected void renderTop(Element e) {
		if(decorate) {
			e.appendChild(createLabel());
		}
	}

}
