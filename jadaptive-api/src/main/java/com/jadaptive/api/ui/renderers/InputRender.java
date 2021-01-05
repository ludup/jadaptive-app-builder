package com.jadaptive.api.ui.renderers;

import org.jsoup.nodes.Element;

public abstract class InputRender {

	protected final String resourceKey;
	protected String defaultValue;
	
	public InputRender(String resourceKey) {
		this.resourceKey = resourceKey;
	}
	
	public abstract Element renderInput();

	protected String replaceResourceKey(String str) {
		return str.replace("${resourceKey}", resourceKey);
	}
}
