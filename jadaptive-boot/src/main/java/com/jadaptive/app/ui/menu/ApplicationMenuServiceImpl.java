package com.jadaptive.app.ui.menu;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.repository.UUIDEntity;
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
					
					String path = m.path();
					String uuid = m.uuid();
					String bundle = m.bundle();
					String i18n = m.i18n();
					
					if(UUIDEntity.class.isAssignableFrom(clz)) {
						try {
							UUIDEntity e = (UUIDEntity) clz.getConstructor().newInstance();
							if(StringUtils.isBlank(path)) {
								path = "/app/ui/search/" + e.getResourceKey();
							}
							if(StringUtils.isBlank(bundle)) {
								bundle = e.getResourceKey();
							}
							if(StringUtils.isBlank(uuid)) {
								uuid = UUID.randomUUID().toString();
							}
							if(StringUtils.isBlank(i18n)) {
								i18n = e.getResourceKey() + ".names";
							}
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						}
					}
					annotatedMenus.add(new DynamicMenu(m, path, bundle, uuid, i18n));
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
		
		String path;
		String bundle;
		String uuid;
		String i18n;
		PageMenu m;
		
		DynamicMenu(PageMenu m, String path, String bundle, String uuid, String i18n) {
			this.m = m;
			this.path = path;
			this.bundle = bundle;
			this.uuid = uuid;
			this.i18n = i18n;
		}
		
		@Override
		public Integer weight() {
			return m.weight();
		}
		
		@Override
		public String getUuid() {
			return uuid;
		}
		
		@Override
		public String getPath() {
			return path;
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
			return i18n;
		}
		
		@Override
		public String getBundle() {
			return bundle;
		}
	}
}
