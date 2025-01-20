package com.jadaptive.api.ui.forms;

import org.jsoup.nodes.Element;

public abstract class HtmlInputField extends AbstractInputField {

	private String type;
	
	protected HtmlInputField(String bundle, String resourceKey, String formVariable, boolean readonly, String type) {
		super(bundle, resourceKey, formVariable, readonly);
		this.type = type;
	}
	
	@Override
	protected void doInput(Element e, String value) {
		Element i = e.selectFirst("jad\\:input");
		input(i,  type, value);
	}

	@Override
	public Class<?> getResourceClass() {
		return HtmlInputField.class;
	}

	
}
