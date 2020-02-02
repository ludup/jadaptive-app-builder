package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
		
		String name();
		
		String resourceKey();
		
		EntityType type();
		
		EntityScope scope() default EntityScope.GLOBAL;

		boolean hidden() default false;
		
		boolean system() default false;
		
}
