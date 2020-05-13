package com.jadaptive.app.ui.renderers;

import org.jsoup.select.Elements;

import com.jadaptive.api.template.FieldTemplate;

public abstract class InputRender {

	FieldTemplate template;
	public InputRender(Elements rootElement, FieldTemplate template) {
		this.template = template;
		renderInput(rootElement, template);
	}
	
	protected abstract void renderInput(Elements rootElement, FieldTemplate template);

	protected String replaceResourceKey(String str) {
		return str.replace("${resourceKey}", template.getResourceKey());
	}
}
