package com.jadaptive.api.ui.menu;

import java.util.Collection;

public interface ApplicationMenuService {

	String ADMINISTRATION_MENU_UUID = "1ec79cb8-fc68-465c-b447-c6eca31ea2e8";
	String HOME_MENU_UUID = "a6355638-3ff9-4213-81e4-d193282d84bf";
	String RESOURCE_MENU_UUID = "1ae34d5c-c6cd-4f53-be7a-3c23898390f7";
	String SYSTEM_MENU_UUID = "7dc19db3-0547-451f-8f7b-cf5c3e147c97";
	
	Collection<ApplicationMenu> getMenus();

}
