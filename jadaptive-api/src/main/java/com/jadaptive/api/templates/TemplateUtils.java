package com.jadaptive.api.templates;

import java.lang.reflect.Field;
import java.util.Objects;

import com.jadaptive.api.template.ObjectDefinition;

public class TemplateUtils {

	public static String lookupClassResourceKey(Class<?> clz) {
		try {
			Field resourceKeyField = clz.getField("RESOURCE_KEY");
			return (String) resourceKeyField.get(null);
		} catch(Throwable e) {
			throw new IllegalStateException("Missing RESOURCE_KEY attribute from OBJECT_REFERENCE typed @ObjectField annotation");
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
}
