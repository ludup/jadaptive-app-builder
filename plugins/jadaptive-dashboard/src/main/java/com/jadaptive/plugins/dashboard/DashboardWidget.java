package com.jadaptive.plugins.dashboard;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.ExtensionPoint;

import com.jadaptive.api.ui.BasicDashboardTypes;
import com.jadaptive.api.ui.DashboardType;

public interface DashboardWidget extends ExtensionPoint {

	String getIcon();
	
	default String getIconGroup() {
		return "fa-solid";
	}
	
	String getBundle();
	
	String getName();
	
	void renderWidget(Document document, Element element);

	Integer weight();

	boolean wantsDisplay();
	
	default boolean hasHelp() { 
		return false;
	}
	
	default DashboardType getType() {
		return BasicDashboardTypes.SERVER_INFORMATION;
	}
	
	default WidgetPlacement getPlacement() {
		return WidgetPlacement.STANDARD;
	}
}
