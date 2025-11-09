package com.jadaptive.api.auth;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticationConfig {

	String resourceKey();

	String icon();

	String iconGroup() default "fa-solid";

	String bundle() default "";

}
