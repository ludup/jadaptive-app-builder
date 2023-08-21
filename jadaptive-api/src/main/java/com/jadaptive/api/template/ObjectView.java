package com.jadaptive.api.template;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface ObjectView {
	String value();

	int weight() default 0;
	
	FieldRenderer renderer() default FieldRenderer.DEFAULT;

	String bundle() default "";

	boolean disableEncoding() default false;
	
	boolean disableLabels() default false;
	
	String dependsOn() default "";
	
	String dependsValue() default "";
	
	boolean autosave() default false;
	
}
