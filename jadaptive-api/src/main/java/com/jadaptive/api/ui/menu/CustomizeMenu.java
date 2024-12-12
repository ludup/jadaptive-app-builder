package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

@Component
public class CustomizeMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "customize.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "";
	}

	@Override
	public String getIcon() {
		return "fa-paintbrush";
	}

	@Override
	public String getParent() {
		return null;
	}

	@Override
	public String getUuid() {
		return ApplicationMenuService.CUSTOMIZE_MENU;
	}

	@Override
	public Integer weight() {
		return 500;
	}
	
	public Collection<String> getPermissions() { 
		return Arrays.asList("tenant.read");
	}
	
	

}
