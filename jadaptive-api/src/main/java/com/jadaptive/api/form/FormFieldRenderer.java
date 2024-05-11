package com.jadaptive.api.form;

import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

public interface FormFieldRenderer extends ExtensionPoint {

	void render(Element e, String bundle, String formVariable, String value);
}
