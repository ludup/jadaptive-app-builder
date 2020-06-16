package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ObjectDefinition {
		
		@Deprecated
		String name();
		
		String resourceKey();
		
		ObjectType type() default ObjectType.COLLECTION;
		
		ObjectScope scope() default ObjectScope.GLOBAL;

		boolean hidden() default false;
		
		boolean system() default true;
		
		boolean recurse() default true;
		
		String[] aliases() default { };
		
		String defaultFilter() default "";
}
