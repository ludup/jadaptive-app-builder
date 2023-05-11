package com.jadaptive.api.ui.menu;

import org.springframework.stereotype.Component;

import com.jadaptive.api.user.User;

@Component
public class UsersMenu extends AbstractSearchPageMenu {

	public static final String UUID = "c8d39ed7-9f9d-484b-8305-04d1743d47ab";
	
	public UsersMenu() {
		super(User.RESOURCE_KEY, "userInterface", "fa-users", ApplicationMenuService.SECURITY_MENU_UUID, -10, UUID);
	}
}