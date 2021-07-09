package com.jadaptive.api.ui;

import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

public interface DashboardWidget extends ExtensionPoint {

	String getIcon();
	
	String getBundle();
	
	String getName();
	
	void renderWidget(Element element);

	Integer weight();
}
