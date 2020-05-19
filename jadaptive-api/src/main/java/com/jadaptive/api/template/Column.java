package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
		
		FieldType type();	
		
		String name();
		
		String defaultValue() default "";

		boolean readOnly() default false;
		
		boolean required() default false;

		boolean hidden() default false;

		boolean manualEncryption() default false;
		
		boolean automaticEncryption() default false;
		
		boolean searchable() default false;
		
		boolean unique() default false;
		
		boolean textIndex() default false;
		
		String references() default "";

		// USER INTERFACE PROPERTIES
		String description();
		
}
