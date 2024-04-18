package com.jadaptive.api.templates;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
@Repeatable(value = ObjectDynamicFields.class)
public @interface ObjectDynamicField {

	String field();
	
	String dependsOn();
	
	String dependsValue();
}
