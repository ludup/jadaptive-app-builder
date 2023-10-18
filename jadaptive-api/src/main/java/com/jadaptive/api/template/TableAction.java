package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableAction  {
	
	enum Target { TABLE, ROW };
	
	enum Window { SELF, BLANK }
	
	Target target() default Target.TABLE;
	
	Window window() default Window.SELF;
	
	String bundle(); 
	
	String resourceKey();
	
	String url();

	String buttonClass() default "primary";
	
	String icon();
	
	String iconGroup() default "fa-solid";
	
	boolean writeAction() default false;
	
	boolean confirmationRequired() default false;
	
	String targetKey() default "";


}
