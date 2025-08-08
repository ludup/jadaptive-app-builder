package com.jadaptive.api.templates;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.jadaptive.api.template.ObjectDefinition;

public class TemplateUtils {

	public static String lookupClassResourceKey(Class<?> clz) {
		try {
			Field resourceKeyField = clz.getField("RESOURCE_KEY");
			return (String) resourceKeyField.get(null);
		} catch(Throwable e) {
			ObjectDefinition def = clz.getAnnotation(ObjectDefinition.class);
			if(Objects.nonNull(def)) {
				return def.resourceKey();
			}
			throw new IllegalStateException("Missing RESOURCE_KEY attribute from OBJECT_REFERENCE typed @ObjectField annotation", e);
		}
	}
	
	public static Class<?> getBaseClass(Class<?> clz) {
		
		Class<?> parent = clz.getSuperclass();
		Class<?> templateBase = null;
		while(parent!=null){
			
			ObjectDefinition t = parent.getAnnotation(ObjectDefinition.class);
			if(Objects.nonNull(t)) {
				templateBase = parent;
			}
			parent = parent.getSuperclass();
		}
		
		return templateBase;
	}

	public static String lookupClassResourceKeyWithDefault(Class<?> clz, String resourceKey) {
		try {
			Field resourceKeyField = clz.getField("RESOURCE_KEY");
			return (String) resourceKeyField.get(null);
		} catch(Throwable e) {
			ObjectDefinition def = clz.getAnnotation(ObjectDefinition.class);
			if(Objects.nonNull(def)) {
				return def.resourceKey();
			}
			return resourceKey;
		}
	}
	
	public static String lookupBundleWithDefault(Class<?> clz, String bundle) {
		
		String resourceKey = lookupClassResourceKeyWithDefault(clz, bundle);
		ObjectDefinition def = clz.getAnnotation(ObjectDefinition.class);
		if(Objects.nonNull(def)) {
			if(StringUtils.isNotBlank(def.bundle())) {
				return def.bundle();
			} else {
				return def.resourceKey();
			}
		}
		return resourceKey;
	}

	public static String resolveRootBundle(List<Class<?>> classes) {
		for(Class<?> clz : classes) {
			ObjectDefinition def = clz.getAnnotation(ObjectDefinition.class);
			if(Objects.nonNull(def)) {
				if(StringUtils.isNotBlank(def.bundle())) {
					return def.bundle();
				} else {
					return def.resourceKey();
				}
			}
		}
		return "default";
	}
}
