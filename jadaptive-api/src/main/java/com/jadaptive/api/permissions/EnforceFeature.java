package com.jadaptive.api.permissions;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface EnforceFeature {
	
	public final static String UNLICENSED = "unlicensed";
	
	String value();
}
