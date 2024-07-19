package com.jadaptive.app.ui.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.templates.TemplateUtils;
import com.jadaptive.api.tenant.FeatureEnablementService;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuExtender;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;
import com.jadaptive.utils.Instrumentation;

@Service
public class ApplicationMenuServiceImpl extends AuthenticatedService implements ApplicationMenuService { 
	
	public static final String MENU_CACHE = "menuCache";
	@Autowired
	private ApplicationService applicationService; 

	@Autowired
	private ClassLoaderService classService; 
	
	@Autowired
	private PermissionService permissionService;
	
	private List<ApplicationMenu> annotatedMenus = null;
	
	public Collection<ApplicationMenu> getMenus() {
		
		@SuppressWarnings("unchecked")
		Collection<ApplicationMenu> tmp = (Collection<ApplicationMenu>) Request.get().getSession().getAttribute(MENU_CACHE);
		if(Objects.nonNull(tmp)) {
			return tmp;
		}
		
		List<ApplicationMenu> results = new ArrayList<>();
		List<ApplicationMenu> menus = new ArrayList<>();
		
		if(Objects.isNull(annotatedMenus)) {
		
			annotatedMenus = new ArrayList<>();
			for(Class<?> clz : classService.resolveAnnotatedClasses(PageMenu.class)) {
				PageMenu[] pageMenus = clz.getAnnotationsByType(PageMenu.class);
				if(Objects.nonNull(pageMenus)) {
					for(PageMenu m : pageMenus) {
						
						boolean enabled = true;
						if(StringUtils.isNotBlank(m.feature())) {
							try {
								enabled = ApplicationServiceImpl.getInstance().getBean(FeatureEnablementService.class).isEnabled(m.feature());
							} catch(NoSuchBeanDefinitionException e) {
								enabled = false;
							}
						}
						String path = m.path();
						String uuid = m.uuid();
						String bundle = m.bundle();
						String i18n = m.i18n();
						
						if(UUIDEntity.class.isAssignableFrom(clz)) {
							try {
								String resourceKey = TemplateUtils.lookupClassResourceKey(clz);
								if(StringUtils.isBlank(path)) {
									path = "/app/ui/search/" + resourceKey;
								}
								if(StringUtils.isBlank(bundle)) {
									bundle = resourceKey;
								}
								if(StringUtils.isBlank(uuid)) {
									uuid = UUID.randomUUID().toString();
								}
								if(StringUtils.isBlank(i18n)) {
									i18n = resourceKey + ".names";
								}
							} catch (IllegalArgumentException | SecurityException e) {
							}
						}
						annotatedMenus.add(new DynamicMenu(m, path, bundle, uuid, i18n, enabled));
					}
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
		
		Request.get().getSession().setAttribute(MENU_CACHE, tmp = Collections.unmodifiableCollection(results));
		return tmp;
	}
	
	@Override
	public boolean checkPermission(ApplicationMenu m) {
		try(var ptimed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission(" + m.getI18n() + ")")) {
			try(var timed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission.with(" + m.getI18n() + ")")) {
				for(String perm : m.getPermissions()) {
					if(StringUtils.isNotBlank(perm)) {
						try {
							permissionService.assertPermission(perm);
						} catch(AccessDeniedException e) { 
							return false;
						}
					}
				}
			}
	
			try(var timed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission.without(" + m.getI18n() + ")")) {
				for(String perm : m.getWithoutPermissions()) {
					if(StringUtils.isNotBlank(perm)) {
						try {
							permissionService.assertPermission(perm);
							return false;
						} catch(AccessDeniedException e) { 
						}
					}
				}
			}
			
			return true;
		}
	}
	
	@Override
	public boolean checkPermission(ApplicationMenu m, Set<String> resolvedPermissions, boolean administrator) {
		try(var ptimed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission(" + m.getI18n() + ")")) {
			try(var timed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission.with(" + m.getI18n() + ")")) {
				for(String perm : m.getPermissions()) {
					if(StringUtils.isNotBlank(perm)) {
						try {
							if(!administrator) {
								permissionService.assertAnyResolvedPermission(resolvedPermissions, perm);
							}
						} catch(AccessDeniedException e) { 
							return false;
						}
					}
				}
			}
	
			try(var timed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission.without(" + m.getI18n() + ")")) {
				for(String perm : m.getWithoutPermissions()) {
					if(StringUtils.isNotBlank(perm)) {
						try {
							if(!administrator) {
								permissionService.assertAnyResolvedPermission(resolvedPermissions, perm);
							}
							return false;
						} catch(AccessDeniedException e) { 
						}
					}
				}
			}
			
			return true;
		}
	}
	
	class DynamicMenu implements ApplicationMenu {
		
		String path;
		String bundle;
		String uuid;
		String i18n;
		boolean enabled;
		PageMenu m;
		
		DynamicMenu(PageMenu m, String path, String bundle, String uuid, String i18n, boolean enabled) {
			this.m = m;
			this.path = path;
			this.bundle = bundle;
			this.uuid = uuid;
			this.i18n = i18n;
			this.enabled = enabled;
		}
		
		@Override
		public boolean isEnabled() {
			return enabled;
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

		@Override
		public Collection<String> getPermissions() {
			return Arrays.asList(m.withPermission().split(","));
		}

		@Override
		public Collection<String> getWithoutPermissions() {
			return Arrays.asList(m.withoutPermission().split(","));
		}
		
		
		
	}
}
