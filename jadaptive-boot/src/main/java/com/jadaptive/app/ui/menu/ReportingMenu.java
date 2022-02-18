package com.jadaptive.app.ui.menu;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class ReportingMenu implements ApplicationMenu {

	@Autowired
	private ProductService productService; 
	
	@Override
	public String getUuid() {
		return ApplicationMenuService.REPORTING_MENU_UUID;
	}
	
	public boolean isEnabled() { return productService.supportsFeature("eventLog"); }
	
	@Override
	public String getResourceKey() {
		return "reporting.name";
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
	public Collection<String> getPermissions() {
		return Collections.emptyList();
	}

	@Override
	public String getIcon() {
		return "cabinet-filing";
	}

	@Override
	public String getParent() {
		return null;
	}
	
	@Override
	public Integer weight() {
		return 9999;
	}

}
