package com.jadaptive.api.scheduler;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface TenantTaskConfig {
	
	boolean logging() default false;
	
	boolean allowTenantCancel() default false;
	
	boolean allowTenantRunNow() default false;
	
	boolean allowCancel() default false;
	
	boolean allowRunNow() default false;
	
}
