package com.jadaptive.plugins.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuExtender;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Service
public class ApplicationMenuServiceImpl implements ApplicationMenuService { 
	
	@Autowired
	private ApplicationService applicationService; 

	public Collection<ApplicationMenu> getMenus() {
		
		List<ApplicationMenu> menus = new ArrayList<>();
		for(ApplicationMenu menu :  applicationService.getBeans(ApplicationMenu.class)) {
			boolean extended = false;
			for(ApplicationMenuExtender ext : applicationService.getBeans(ApplicationMenuExtender.class)) {
				if(ext.isExtending(menu)) {
					extended = true;
					if(ext.isVisible(menu)) {
						menus.add(menu);
						break;
					}
				}
			}
			if(!extended && menu.isVisible()) {
				menus.add(menu);
			}
		}
		return Collections.unmodifiableCollection(menus);
	}
}
