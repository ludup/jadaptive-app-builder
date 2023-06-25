package com.jadaptive.api.ui.pages.config;

import com.jadaptive.api.config.ConfigurationPageItem;

public class DynamicConfigurationItem implements ConfigurationPageItem {

	ConfigurationItem i;
	
	String resourceKey;
	String path;
	String bundle;
	boolean system;
	
	public DynamicConfigurationItem(ConfigurationItem i, String resourceKey, String path, String bundle, boolean system) {
		this.i = i;
		this.resourceKey = resourceKey;
		this.path = path;
		this.bundle = bundle;
		this.system = system;
	}
	
	
	@Override
	public String getResourceKey() {
		return resourceKey;
	}

	@Override
	public String getBundle() {
		return bundle;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getIcon() {
		return i.icon();
	}

	@Override
	public Integer weight() {
		return i.weight();
	}


	@Override
	public boolean isSystem() {
		return system;
	}
	
	

}
