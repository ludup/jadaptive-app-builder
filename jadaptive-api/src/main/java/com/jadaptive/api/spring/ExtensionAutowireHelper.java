package com.jadaptive.api.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.jadaptive.api.app.AutowiredExtension;

public class ExtensionAutowireHelper {

	static Logger log = LoggerFactory.getLogger(ExtensionAutowireHelper.class);
	
    public static void autowiredExtensions(Object bean, List<ApplicationContext> parentContexts) {
		
		for(Field field : findFields(bean.getClass(), AutowiredExtension.class)) {
			try {
				field.setAccessible(true);
				if(Objects.isNull(field.get(bean))) {
					field.set(bean, findExtension(field.getType(), parentContexts));
					if(log.isInfoEnabled()) {
						log.info("Autowired {} Extension on {}", field.getType().getSimpleName(), bean.getClass().getSimpleName());
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
	}

	public static Object findExtension(Class<?> type, List<ApplicationContext> parentContexts) {
		
		for(ApplicationContext ctx : parentContexts) {
			try {
				return ctx.getBean(type);
			} catch(Throwable e) {
			}
		}
		
		throw new IllegalStateException("Cannot find a instance of " + type.getSimpleName() + " within the extensions parent application contexts");
	}
	
	/**
	 * https://stackoverflow.com/questions/16585451/get-list-of-fields-with-annotation-by-using-reflection
	 */
	public static Set<Field> findFields(Class<?> classs, Class<? extends Annotation> ann) {
	    Set<Field> set = new HashSet<>();
	    Class<?> c = classs;
	    while (c != null) {
	        for (Field field : c.getDeclaredFields()) {
	            if (field.isAnnotationPresent(ann)) {
	                set.add(field);
	            }
	        }
	        c = c.getSuperclass();
	    }
	    return set;
	}
}
