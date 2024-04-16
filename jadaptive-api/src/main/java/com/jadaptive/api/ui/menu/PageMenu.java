package com.jadaptive.api.ui.menu;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
@Repeatable(PageMenus.class)
public @interface PageMenu {

	String parent() default "";

	int weight() default 9999;

	String uuid() default "";

	String path() default "";

	String icon();

	String i18n() default "";

	String bundle() default "";
	
	String withPermission() default "";
	
	String withoutPermission() default "";
	
	String feature() default "";
	
}
