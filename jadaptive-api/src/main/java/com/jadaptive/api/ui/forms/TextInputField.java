package com.jadaptive.api.ui.forms;

import org.jsoup.nodes.Element;
import org.pf4j.Extension;

@Extension
public class TextInputField extends HtmlInputField {

	public TextInputField(String bundle, String resourceKey, String formVariable, boolean readonly) {
		super(bundle, resourceKey, formVariable, readonly, "text");
	}

}
