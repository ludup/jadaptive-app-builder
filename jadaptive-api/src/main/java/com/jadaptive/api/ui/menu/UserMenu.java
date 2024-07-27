package com.jadaptive.api.ui.menu;

import java.util.Collection;
import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class UserMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "userMenu.name";
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
		return "fa-user";
	}

	@Override
	public String getParent() {
		return null;
	}

	@Override
	public String getUuid() {
		return ApplicationMenuService.USER_MENU;
	}

	@Override
	public Integer weight() {
		return Integer.MIN_VALUE;
	}
	
	public Collection<String> getPermissions() { 
		return Collections.emptyList();
	}
	
	

}
