package com.jadaptive.app.ui.menu;

import static com.jadaptive.utils.Instrumentation.timed;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.templates.TemplateUtils;
import com.jadaptive.api.tenant.FeatureEnablementService;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuExtender;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.NoPageMenuFilter;
import com.jadaptive.api.ui.menu.PageMenu;
import com.jadaptive.api.ui.menu.PageMenuFilter;
import com.jadaptive.utils.Instrumentation;

@Service
public class ApplicationMenuServiceImpl extends AuthenticatedService implements ApplicationMenuService { 
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApplicationMenuServiceImpl.class);
	
	public static final String MENU_CACHE = "menuCache";
	@Autowired
	private App applicationService; 

	@Autowired
	private ClassLoaderService classService; 
	
	@Autowired
	private PermissionService permissionService;
	
	private List<ApplicationMenu> annotatedMenus = null;
	
	public Collection<ApplicationMenu> getMenus() {
		
		if(Session.getOr().isPresent()) {
			@SuppressWarnings("unchecked")
			Collection<ApplicationMenu> tmp = (Collection<ApplicationMenu>) Session.get().getAttribute(MENU_CACHE);
			if(Objects.nonNull(tmp)) {
				return tmp;
			}
		}
		
		List<ApplicationMenu> results = new ArrayList<>();
		List<ApplicationMenu> menus = new ArrayList<>();
		
		if(Objects.isNull(annotatedMenus)) {
		
			annotatedMenus = new ArrayList<>();
			for(Class<?> clz : classService.resolveAnnotatedClasses(PageMenu.class)) {
				PageMenu[] pageMenus = clz.getAnnotationsByType(PageMenu.class);
				if(Objects.nonNull(pageMenus)) {
					for(PageMenu m : pageMenus) {
						
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
						if(Boolean.getBoolean("jadaptive.development")) {
							log.info("Adding menu {} with path {} and bundle {}", i18n, path, bundle);
						}
						annotatedMenus.add(new DynamicMenu(m, path, bundle, uuid, i18n));
					}
				}
			}
		}
		
		menus.addAll(annotatedMenus);
		menus.addAll(applicationService.getBeans(ApplicationMenu.class));
		
		/* Remove duplicate IDs */
//		var ids = new HashSet<String>();
//		var it = menus.stream().sorted((o1, o2) -> o1.weight().compareTo(o2.weight())).iterator();
//		while(it.hasNext()) {
//			var nxt = it.next();
//			var id = nxt.getI18n();
//			if(ids.contains(id)) {
//				menus.remove(nxt);
//				continue;
//			}
//			else {
//				ids.add(id);
//			}
//		}
		
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
		
		var newtmp = Collections.unmodifiableCollection(results);
		Session.getOr().ifPresent(s -> s.setAttribute(MENU_CACHE, newtmp));
		return newtmp;
	}
	
	@Override
	public boolean checkPermission(ApplicationMenu m) {
		try(@SuppressWarnings("unused")
		var ptimed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission(" + m.getI18n() + ")")) {
			try(@SuppressWarnings("unused")
			var timed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission.with(" + m.getI18n() + ")")) {
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
	
			try(@SuppressWarnings("unused")
			var timed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission.without(" + m.getI18n() + ")")) {
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
		try(@SuppressWarnings("unused")
		var ptimed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission(" + m.getI18n() + ")")) {
			try(@SuppressWarnings("unused")
			var timed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission.with(" + m.getI18n() + ")")) {
				for(String perm : m.getPermissions()) {
					if(StringUtils.isNotBlank(perm)) {
						try {
							permissionService.assertAnyResolvedPermission(resolvedPermissions, perm);
						} catch(AccessDeniedException e) { 
							return false;
						}
					}
				}
			}
	
			try(@SuppressWarnings("unused")
			var timed = Instrumentation.timed("ApplicationMenuServiceImpl#checkPermission.without(" + m.getI18n() + ")")) {
				for(String perm : m.getWithoutPermissions()) {
					if(StringUtils.isNotBlank(perm)) {
						try {
							permissionService.assertAnyResolvedPermission(resolvedPermissions, perm);
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
		PageMenu m;	
		
		Map<Class<?>,PageMenuFilter> filters = new HashMap<>();
		
		
		DynamicMenu(PageMenu m, String path, String bundle, String uuid, String i18n) {
			this.m = m;
			this.path = path;
			this.bundle = bundle;
			this.uuid = uuid;
			this.i18n = i18n;
		}
		
		@SuppressWarnings("unused")
		@Override
		public boolean isEnabled() {
			var enabledNow = true;
			
			if(StringUtils.isNotBlank(m.feature())) {
				if(!App.bean(FeatureEnablementService.class).isEnabled(m.feature())) {
					enabledNow = false;
				}
			}
			
			if(enabledNow) {
				if(!m.filter().equals(NoPageMenuFilter.class)) {
					try(var perms = timed("ApplicationMenuServiceImpl.DynamicMenu.isEnabled")) {
						PageMenuFilter filter = getFilter();
						try(var sa = timed("ApplicationMenuServiceImpl.DynamicMenu.isEnabled." + filter.getClass().getSimpleName())) {
							if(!filter.isEnabled(this)) {
								enabledNow = false;
							}
						}
					}
				}
			}
			
			return enabledNow;
		}

		@Override
		public boolean isVisible() {
			var visibleNow = true;
			if(visibleNow) {
				if(!m.filter().equals(NoPageMenuFilter.class)) {
					try(@SuppressWarnings("unused")
					var perms = timed("ApplicationMenuServiceImpl.DynamicMenu.isVisible")) {
						var filter = getFilter();
						try(@SuppressWarnings("unused")
						var sa = timed("ApplicationMenuServiceImpl.DynamicMenu.isVisible." + filter.getClass().getSimpleName())) {
							if(!filter.isVisible(this)) {
								visibleNow = false;
							}
						}
					}
				}
			}
			
			return visibleNow;
		}

		private PageMenuFilter getFilter() {
			PageMenuFilter filter = filters.get(m.filter());
			if(Objects.isNull(filter)) {
				try {
					filter = applicationService.autowire(m.filter().getConstructor().newInstance());
					filters.put(m.filter(), filter);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			return filter;
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
			return StringUtils.isBlank(m.parent()) ? null : m.parent();
		}
		
		@Override
		public String getIcon() {
			return m.icon();
		}
		
		@Override
		public String getIconGroup() {
			return m.iconGroup();
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
