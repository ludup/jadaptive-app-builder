package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.ui.pages.About;
import com.jadaptive.api.ui.pages.Welcome;
import com.jadaptive.api.ui.pages.auth.Login;
import com.jadaptive.utils.FileUtils;
import com.jadaptive.utils.Utils;

@Component
public class PageCache {

	static Logger log = LoggerFactory.getLogger(PageCache.class);
	
	@Autowired
	private ApplicationService applicationService;
	
	@Autowired
	private PermissionService permissionsService;
	
	Map<Class<? extends PageExtension>,PageExtension> extensionCache = new HashMap<>();
	Map<String,PageExtension> extensionsByName = new HashMap<>();
	Map<String,Page> aliasCache = new HashMap<>();
	Map<Class<? extends Page>, Page> pageCache = new HashMap<>();
//	Class<? extends Page> homePage;
	private Class<? extends Page> defaultPage = Login.class;
	
	
	public Page resolvePage(String resourceUri, boolean processParameters) throws FileNotFoundException, AccessDeniedException {
		
		String name = FileUtils.firstPathElement(resourceUri);
		if(StringUtils.isBlank(name)) {
			return getHomePage();
		}
		Page cachedPage = aliasCache.get(name);
		if(Objects.nonNull(cachedPage)) {
			if(processParameters && ReflectionUtils.hasAnnotation(cachedPage.getClass(), RequestPage.class)) {
				Page page = createNewInstance(resourceUri, cachedPage);
				postCreation(page);
				page.onCreate();
				return page;
			}
			return cachedPage;
		}
		try {
			Collection<Page> pages = applicationService.getBeans(Page.class);
			for(Page page : pages) {
				if(page.getUri().contentEquals(resourceUri) || page.getUri().contentEquals(name) || page.getClass().getName().equalsIgnoreCase(name)) {
					aliasCache.put(page.getUri(), page);
					pageCache.put(page.getClass(), page);
					if(processParameters && ReflectionUtils.hasAnnotation(page.getClass(), RequestPage.class)) {
						page = createNewInstance(resourceUri, page);
					}
					postCreation(page);
					page.onCreate();
					return page;
				}
			}
		} catch(NoSuchBeanDefinitionException e) { }
		
		throw new FileNotFoundException(resourceUri);
		
	}
	
	public Class<? extends Page> resolvePageClass(String resourceUri) throws FileNotFoundException {
		
		String name = FileUtils.firstPathElement(resourceUri);
		if(StringUtils.isBlank(name)) {
			return getHomePage().getClass();
		}
		Page cachedPage = aliasCache.get(name);
		if(Objects.nonNull(cachedPage)) {
			return cachedPage.getClass();
		}
		try {
			Collection<Page> pages = applicationService.getBeans(Page.class);
			for(Page page : pages) {
				if(page.getUri().contentEquals(name) || page.getClass().getName().equalsIgnoreCase(name)) {
					return page.getClass();
				}
			}
		} catch(NoSuchBeanDefinitionException e) { }
		
		throw new FileNotFoundException();
	}
	
	private Page createNewInstance(String resourceUri, Page cachedPage) throws FileNotFoundException {
		try {
			Page page = cachedPage.getClass().getConstructor().newInstance();
			applicationService.autowire(page);
			
			Map<String,Object> vars = urlPathVariables( 
					ReflectionUtils.getAnnotation(page.getClass(), RequestPage.class).path(),
					resourceUri);
			vars.put("resourcePath", resourceUri);
			
			populateFields(page, vars);
			
			return page;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	private void postCreation(Page page) throws FileNotFoundException {
		try {
			Method m = ReflectionUtils.getMethod(page.getClass(), "created");
			if(log.isDebugEnabled()) {
				log.debug("Calling created on " + page.getClass().getName());
			}
			m.invoke(page);
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("Failed to call created method", e);
		} catch (InvocationTargetException e) {
			if(e.getTargetException() instanceof FileNotFoundException) {
				throw (FileNotFoundException) e.getTargetException();
			}
			if(e.getTargetException() instanceof Redirect) {
				throw (Redirect) e.getTargetException();
			}
			if(e.getTargetException() instanceof ObjectNotFoundException) {
				throw new FileNotFoundException();
			}
			log.error("Failed to call created method", e);
		} 
	}
	
	public void populateFields(Page page, Map<String, Object> vars) {
		
		for(Map.Entry<String,Object> e : vars.entrySet()) { 
			try {
				Field field = ReflectionUtils.getField(page.getClass(), e.getKey());
				field.setAccessible(true);

				if(field.getType().isAssignableFrom(Integer.class)) {
					field.set(page, Integer.parseInt(e.getValue().toString()));
				} else {
					field.set(page, e.getValue());
				}
				
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
				log.error("Failed to populate path arg {}", e.getKey(), e);
			}
			
		}
		
	}

	public static Map<String, Object> urlPathVariables(String[] matchPaths, String requestPath) throws FileNotFoundException {
		
		
		for(String matchPath : matchPaths) {
		
			Map<String, Object> vars = new LinkedHashMap<>();
			/**
			 * If there are path variables in the raw path, then extract the values of these
			 * from the request path.
			 */
			if (matchPath.indexOf('{') != -1) {
				String[] t1 = requestPath.split("/");
				String[] t2 = matchPath.split("/");
				if (t1.length != t2.length)
					continue;
				for (int i = 0; i < t1.length; i++) {
					int eidx = t2[i].indexOf('}');
					int sidx = t2[i].indexOf('{');
					if (eidx != 1 && sidx != -1) {
						if (sidx > 0 || (eidx != t2[i].length() - 1 && eidx != -1)) {
							log.warn("View path %s matched the request path {}, but element {} did not consist solely of a path variable.",
									requestPath, matchPath);
							continue;
						}
							
						String name = t2[i].substring(1, t2[i].length() - 1);
						String val = t1[i];
						vars.put(name, val);
					}
				}
			}
			return vars;
		}
		
		throw new FileNotFoundException(String.format(
				"Unable to match request URI %s with page paths %s",
				requestPath,
				Utils.csv(matchPaths)));
	}
	
	public static String ensureRoot(String path) {
		if (path == null || path.length() == 0)
			return "/";
		else if (path.startsWith("/"))
			return path;
		else
			return "/" + path;
	}

	public Page resolvePage(Class<? extends Page> clz) throws FileNotFoundException {
		Page cachedPage = pageCache.get(clz);
		if(Objects.nonNull(cachedPage)) {
			return cachedPage;
		}
		try {
			Collection<Page> pages = applicationService.getBeans(Page.class);
			for(Page page : pages) {
				if(page.getClass().equals(clz)) {
					aliasCache.put(page.getUri(), page);
					postCreation(page);
					pageCache.put(page.getClass(), page);
					return page;
				}
			}
		} catch(NoSuchBeanDefinitionException e) { }
		
		throw new FileNotFoundException();
	}
	
	public Page getPage(Class<? extends Page> clz) {
		Page cachedPage = pageCache.get(clz);
		if(Objects.nonNull(cachedPage)) {
			return cachedPage;
		}
		try {
			Collection<Page> pages = applicationService.getBeans(Page.class);
			for(Page page : pages) {
				if(page.getClass().equals(clz)) {
					aliasCache.put(page.getUri(), page);
					postCreation(page);
					pageCache.put(page.getClass(), page);
					return page;
				}
			}
		} catch(NoSuchBeanDefinitionException | FileNotFoundException e) { }
		
		throw new IllegalStateException(String.format("Unknown page %s", clz.getSimpleName()));
	}
	
	public PageExtension resolveExtension(String name) {
		PageExtension cachedExtension = extensionsByName.get(name);
		if(Objects.nonNull(cachedExtension)) {
			return cachedExtension;
		}
		try {
			Collection<PageExtension> extensions = applicationService.getBeans(PageExtension.class);
			for(PageExtension extension : extensions) {
				if(extension.getName().equalsIgnoreCase(name)) {
					extensionCache.put(extension.getClass(), extension);
					extensionsByName.put(name, extension);
					return extension;
				}
			}
		} catch(NoSuchBeanDefinitionException e) { }
		
		throw new IllegalStateException("Cannot resolve page extension " + name);
	}
	
	public PageExtension resolveExtension(Class<? extends PageExtension> clz) {
		PageExtension cachedExtension = extensionCache.get(clz);
		if(Objects.nonNull(cachedExtension)) {
			return cachedExtension;
		}
		try {
			Collection<PageExtension> extensions = applicationService.getBeans(PageExtension.class);
			for(PageExtension extension : extensions) {
				if(extension.getClass().equals(clz)) {
					extensionCache.put(clz, extension);
					extensionsByName.put(extension.getName(), extension);
					return extension;
				}
			}
		} catch(NoSuchBeanDefinitionException e) { }
		
		throw new IllegalStateException();
	}

	public Page getHomePage() throws FileNotFoundException {
		return resolvePage(getHomeClass());
	}
	
	public Class<? extends Page> getHomeClass() {
		var resolvers = applicationService.getBeans(HomePageResolver.class);
		if(resolvers.isEmpty()) {
			log.error("Product does not appear to have any HomePageResolve implementations!");
			return Welcome.class;
		}
		
		for(var resolve : resolvers.
				stream().
				sorted((o1, o2) -> Integer.valueOf(o1.getWeight()).compareTo(o2.getWeight())).
				filter(res -> { 
					try { 
						var perms = res.getPermissions().toArray(new String[0]);
						if(perms.length > 0)
							permissionsService.assertAnyPermission(perms);
						return true;
					} 
					catch(AccessDeniedException ade) {
						return false;
					}
				}).
				toList()) {
			
			var res = resolve.resolve();
			if(res.isPresent()) {
				return res.get();
			}
		}
		if(permissionsService.hasUserContext())
			return About.class;
		else
			return getDefaultPage();
	}


	public static String getPageURL(Page returnTo) {
		return new PageRedirect(returnTo).getUri();
	}

	public void setDefaultPage(Class<? extends Page> defaultPage) {
		this.defaultPage = defaultPage;
	}
	
	public Page resolveDefault() {
		try {
			return resolvePage(defaultPage);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("There is no default page set!");
		}
	}
	
	

	public Class<? extends Page> getDefaultPage() {
		return defaultPage;
	}
}
