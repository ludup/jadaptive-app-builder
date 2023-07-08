package com.jadaptive.api.ui.pages.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface ConfigurationItem {

	String resourceKey() default "";
	
	String bundle() default "";
	
	int weight() default 0;
	
	String icon();
	
	String iconGroup() default "fa-solid";
	
	String path() default "";
	
	boolean system() default false;
}
