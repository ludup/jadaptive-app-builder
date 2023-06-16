package com.jadaptive.app.ui.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuExtender;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;

@Service
public class ApplicationMenuServiceImpl implements ApplicationMenuService { 
	
	@Autowired
	private ApplicationService applicationService; 

	@Autowired
	private ClassLoaderService classService; 
	
	private List<ApplicationMenu> annotatedMenus = null;
	
	public Collection<ApplicationMenu> getMenus() {
		
		List<ApplicationMenu> results = new ArrayList<>();
		List<ApplicationMenu> menus = new ArrayList<>();
		
		if(Objects.isNull(annotatedMenus)) {
		
			annotatedMenus = new ArrayList<>();
			for(Class<?> clz : classService.resolveAnnotatedClasses(PageMenu.class)) {
				PageMenu m = clz.getAnnotation(PageMenu.class);
				if(Objects.nonNull(m)) {
					annotatedMenus.add(new DynamicMenu(m));
				}
			}
		}
		
		menus.addAll(annotatedMenus);
		menus.addAll(applicationService.getBeans(ApplicationMenu.class));
		
		for(ApplicationMenu menu :  menus) {
			boolean extended = false;
			for(ApplicationMenuExtender ext : applicationService.getBeans(ApplicationMenuExtender.class)) {
				if(ext.isExtending(menu)) {
					extended = true;
					if(ext.isVisible(menu)) {
						results.add(menu);
						break;
					}
				}
			}
			if(!extended && menu.isVisible()) {
				results.add(menu);
			}
		}
		return Collections.unmodifiableCollection(results);
	}
	
	class DynamicMenu implements ApplicationMenu {
		
		PageMenu m;
		
		DynamicMenu(PageMenu m) {
			this.m = m;
		}
		
		@Override
		public Integer weight() {
			return m.weight();
		}
		
		@Override
		public String getUuid() {
			return m.uuid();
		}
		
		@Override
		public String getPath() {
			return m.path();
		}
		
		@Override
		public String getParent() {
			return m.parent();
		}
		
		@Override
		public String getIcon() {
			return m.icon();
		}
		
		@Override
		public String getI18n() {
			return m.i18n();
		}
		
		@Override
		public String getBundle() {
			return m.bundle();
		}
	}
}
