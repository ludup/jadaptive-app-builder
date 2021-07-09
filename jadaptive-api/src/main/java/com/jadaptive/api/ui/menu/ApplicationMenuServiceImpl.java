package com.jadaptive.api.ui.menu;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;

@Service
public class ApplicationMenuServiceImpl implements ApplicationMenuService {

	
	@Autowired
	private ApplicationService applicationService; 

	public Collection<ApplicationMenu> getMenus() {
		return applicationService.getBeans(ApplicationMenu.class);
	}
}
