package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Validators.class)
public @interface Validator {

	String value() default ""; 
	
	ValidationType type();
	
	String i18n() default "";

	String bundle() default "";
}
