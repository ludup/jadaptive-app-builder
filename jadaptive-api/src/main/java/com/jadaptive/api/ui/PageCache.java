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
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.utils.FileUtils;

@Component
public class PageCache {

	static Logger log = LoggerFactory.getLogger(PageCache.class);
	
	@Autowired
	private ApplicationService applicationService; 
	
	Map<Class<? extends PageExtension>,PageExtension> extensionCache = new HashMap<>();
	Map<String,PageExtension> extensionsByName = new HashMap<>();
	Map<String,Page> aliasCache = new HashMap<>();
	Map<Class<? extends Page>, Page> pageCache = new HashMap<>();
	
	public Page resolvePage(String resourceUri) throws FileNotFoundException {
		
		String name = FileUtils.firstPathElement(resourceUri);
		if(StringUtils.isBlank(name)) {
			name = "dashboard";
		}
		Page cachedPage = aliasCache.get(name);
		if(Objects.nonNull(cachedPage)) {
			if(ReflectionUtils.hasAnnotation(cachedPage.getClass(), RequestPage.class)) {
				return createNewInstance(resourceUri, cachedPage);
			}
			return cachedPage;
		}
		try {
			Collection<Page> pages = applicationService.getBeans(Page.class);
			for(Page page : pages) {
				if(page.getUri().contentEquals(resourceUri) || page.getUri().contentEquals(name) || page.getClass().getName().equalsIgnoreCase(name)) {
					aliasCache.put(page.getUri(), page);
					pageCache.put(page.getClass(), page);
					if(ReflectionUtils.hasAnnotation(page.getClass(), RequestPage.class)) {
						return createNewInstance(resourceUri, page);
					}
					return page;
				}
			}
		} catch(NoSuchBeanDefinitionException e) { }
		
		throw new FileNotFoundException();
	}
	
	public void registerPage(Page page) {
		aliasCache.put(page.getUri(), page);
	}
	
	public Class<?> resolvePageClass(String resourceUri) throws FileNotFoundException {
		
		String name = FileUtils.firstPathElement(resourceUri);
		if(StringUtils.isBlank(name)) {
			name = "dashboard";
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
			
			try {
				Method m = ReflectionUtils.getMethod(page.getClass(), "created");
				m.invoke(page);
			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
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
			return page;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public void populateFields(Page page, Map<String, Object> vars) {
		
		for(Map.Entry<String,Object> e : vars.entrySet()) { 
			try {
				Field field = ReflectionUtils.getField(page.getClass(), e.getKey());
				field.setAccessible(true);
				field.set(page, e.getValue());
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
				log.error("Failed to populate path arg {}", e.getKey(), e);
			}
			
		}
		
	}

	public static Map<String, Object> urlPathVariables(String matchPath, String requestPath) throws FileNotFoundException {
		Map<String, Object> vars = new LinkedHashMap<>();
		/**
		 * If there are path variables in the raw path, then extract the values of these
		 * from the request path.
		 */
		if (matchPath.indexOf('{') != -1) {
			String[] t1 = requestPath.split("/");
			String[] t2 = matchPath.split("/");
			if (t1.length != t2.length)
				throw new FileNotFoundException(String.format(
						"View path %s matched the request path %s, but they have a different number of path elements.",
						requestPath, matchPath));
			for (int i = 0; i < t1.length; i++) {
				int eidx = t2[i].indexOf('}');
				int sidx = t2[i].indexOf('{');
				if (eidx != 1 && sidx != -1) {
					if (sidx > 0 || (eidx != t2[i].length() - 1 && eidx != -1))
						throw new FileNotFoundException(String.format(
								"View path %s matched the request path %s, but element %s did not consist solely of a path variable.",
								requestPath, matchPath));
					String name = t2[i].substring(1, t2[i].length() - 1);
					String val = t1[i];
					vars.put(name, val);
				}
			}
		}
		return vars;
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
					pageCache.put(page.getClass(), page);
					return page;
				}
			}
		} catch(NoSuchBeanDefinitionException e) { }
		
		throw new FileNotFoundException();
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
}
