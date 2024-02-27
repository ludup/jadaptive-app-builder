package com.jadaptive.api.ui.menu;

import java.util.Collection;
import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class DeveloperMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "developer.name";
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
		return "fa-gear-complex-code";
	}

	@Override
	public String getParent() {
		return null;
	}

	@Override
	public String getUuid() {
		return ApplicationMenuService.DEVELOPER_MENU;
	}

	@Override
	public Integer weight() {
		return 99999;
	}
	
	public Collection<String> getPermissions() { 
		return Collections.emptyList();
	}
	
	

}
