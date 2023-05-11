package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import com.jadaptive.api.permissions.PermissionUtils;

public class AbstractSearchPageMenu implements ApplicationMenu {

	String resourceKey;
	String bundle;
	String icon;
	String parent;
	String uuid = UUID.randomUUID().toString();
	int weight;
	
	protected AbstractSearchPageMenu(String resourceKey, String bundle, String icon, String parent, int weight, String uuid) {
		this.resourceKey = resourceKey;
		this.bundle = bundle;
		this.icon = icon;
		this.parent = parent;
		this.weight = weight;
		this.uuid = uuid;
	}
	
	protected AbstractSearchPageMenu(String resourceKey, String icon, String parent, int weight) {
		this.resourceKey = resourceKey;
		this.bundle = resourceKey;
		this.icon = icon;
		this.parent = parent;
		this.weight = weight;
	}
	
	@Override
	public String getI18n() {
		return String.format("%s.names", resourceKey);
	}

	@Override
	public String getBundle() {
		return bundle;
	}

	@Override
	public Collection<String> getPermissions() { 
		return Arrays.asList(PermissionUtils.getReadPermission(resourceKey)); 
	}
	 
	@Override
	public String getPath() {
		return String.format("/app/ui/search/%s", resourceKey);
	}

	@Override
	public String getIcon() {
		return icon;
	}

	@Override
	public String getParent() {
		return parent;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	@Override
	public Integer weight() {
		return weight;
	}

}
