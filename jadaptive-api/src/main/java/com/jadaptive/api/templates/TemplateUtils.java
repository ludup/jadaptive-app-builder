package com.jadaptive.api.templates;

import java.lang.reflect.Field;

public class TemplateUtils {

	public static String lookupClassResourceKey(Class<?> clz) {
		try {
			Field resourceKeyField = clz.getField("RESOURCE_KEY");
			return (String) resourceKeyField.get(null);
		} catch(Throwable e) {
			throw new IllegalStateException("Missing references attribute from OBJECT_REFERENCE typed @ObjectField annotation");
		}
	}
}
