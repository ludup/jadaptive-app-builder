package com.jadaptive.api.config;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.ExtensionPoint;

public interface ConfigurationPageItem extends ExtensionPoint {

	String getResourceKey();

	String getBundle();

	String getPath();

	default Collection<String> getPermissions() { return Collections.emptyList(); }

	String getIcon();
	
	Integer weight();

	default boolean isEnabled() { return true; }
	
	default boolean isVisible() { return true; }
	
	default boolean isSystem() { return false; }
}
