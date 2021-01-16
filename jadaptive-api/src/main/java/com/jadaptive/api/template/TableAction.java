package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableAction {
	
	enum Target { TABLE, ROW };
	
	Target target() default Target.TABLE;
	
	String bundle(); 
	
	String resourceKey();
	
	String url();

	String buttonClass() default "primary";
}
