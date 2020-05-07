package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Template {
		
		String name();
		
		String resourceKey();
		
		EntityType type() default EntityType.COLLECTION;
		
		EntityScope scope() default EntityScope.GLOBAL;

		boolean hidden() default false;
		
		boolean system() default true;
		
		boolean recurse() default true;
		
		String[] aliases() default { };
}
