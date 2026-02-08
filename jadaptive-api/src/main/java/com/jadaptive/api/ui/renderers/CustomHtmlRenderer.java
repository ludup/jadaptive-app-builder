package com.jadaptive.api.ui.renderers;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.renderers.form.FieldInputRender;

@Component	
public class CustomHtmlRenderer extends FieldInputRender implements Widget {

	public CustomHtmlRenderer() {
	}

	@Override
	public void renderInput(Element element, String fieldValue, boolean readOnly) {
		super.renderInput(element, fieldValue, readOnly);
	}

	@Override
	protected void onRender(Element rootElement, String value, boolean readOnly, String... classes) {
		
	}

}
