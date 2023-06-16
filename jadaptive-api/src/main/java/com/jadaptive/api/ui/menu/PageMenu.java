package com.jadaptive.api.ui.menu;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface PageMenu {

	String parent();

	int weight() default 0;

	String uuid();

	String path();

	String icon();

	String i18n();

	String bundle();
	
}
