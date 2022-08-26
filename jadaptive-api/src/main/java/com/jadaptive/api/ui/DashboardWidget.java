package com.jadaptive.api.ui;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

public interface DashboardWidget extends ExtensionPoint {

	String getIcon();
	
	String getBundle();
	
	String getName();
	
	void renderWidget(Document document, Element element);

	Integer weight();

	boolean wantsDisplay();
	
	default boolean hasHelp() { 
		return false;
	}
}
