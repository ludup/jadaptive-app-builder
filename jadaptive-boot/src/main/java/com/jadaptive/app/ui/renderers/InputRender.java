package com.jadaptive.app.ui.renderers;

import org.jsoup.select.Elements;

public abstract class InputRender {

	String resourceKey;
	String defaultValue;
	
	public InputRender(Elements rootElement, String resourceKey, String defaultValue) {
		this.resourceKey = resourceKey;
		this.defaultValue = defaultValue;
		renderInput(rootElement, resourceKey, defaultValue);
	}
	
	protected abstract void renderInput(Elements rootElement, String resourceKey, String defaultValue);

	protected String replaceResourceKey(String str) {
		return str.replace("${resourceKey}", resourceKey);
	}
}
