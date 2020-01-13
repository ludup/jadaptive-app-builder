package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EntityField {
		
		String defaultValue() default "";
		
		String description();
		
		FieldType type();
		
		boolean required() default false;

		boolean hidden() default false;

}
