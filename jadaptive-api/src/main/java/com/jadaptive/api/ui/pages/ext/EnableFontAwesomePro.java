package com.jadaptive.api.ui.pages.ext;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableFontAwesomePro {
	
	String iconSet() default "fa-regular";
}