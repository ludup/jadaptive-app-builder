package com.jadaptive.api.ui.renderers;

import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

public interface Widget extends ExtensionPoint{

	public void init(String resourceKey,String formVariable, String bundle);
	
	void renderInput(Element element, String fieldValue, boolean readOnly);

	public void load(Element e);

}
