package com.jadaptive.api.ui.menu;

import java.util.Collection;

import org.pf4j.ExtensionPoint;

public interface ApplicationMenu extends ExtensionPoint {

	String getResourceKey();

	String getBundle();

	String getPath();

	Collection<String> getPermissions();

	String getIcon();

	String getParent();

	String getUuid();
	
	Integer weight();

}