package com.jadaptive.api.ui.menu;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.ExtensionPoint;

public interface ApplicationMenu extends ExtensionPoint {

	String getI18n();

	String getBundle();

	String getPath();

	default Collection<String> getPermissions() { return Collections.emptyList(); }

	String getIcon();

	String getParent();

	String getUuid();
	
	Integer weight();

	default boolean isEnabled() { return true; }
	
	default boolean isVisible() { return true; }

	default String getIconGroup() { return "fa-solid"; }
}