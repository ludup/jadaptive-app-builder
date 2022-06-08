package com.jadaptive.api.ui.menu;

import java.util.Collection;

public interface ApplicationMenuService {

	String ADMINISTRATION_MENU_UUID = "1ec79cb8-fc68-465c-b447-c6eca31ea2e8";
	String HOME_MENU_UUID = "a6355638-3ff9-4213-81e4-d193282d84bf";
	String RESOURCE_MENU_UUID = "1ae34d5c-c6cd-4f53-be7a-3c23898390f7";
	String SYSTEM_MENU_UUID = "7dc19db3-0547-451f-8f7b-cf5c3e147c97";
	String REPORTING_MENU_UUID = "21e4615c-82ac-4b85-abf7-81640df4645c";
	String CUSTOMIZE_MENU_UUID = "7c76fc26-971c-4e9b-9077-d4e198b26d3c";
	String CONFIGURATION_MENU = "7f9f9207-6774-4565-95b4-3375abd795b9";
	String SECURITY_MENU = "";

	
	Collection<ApplicationMenu> getMenus();

}
