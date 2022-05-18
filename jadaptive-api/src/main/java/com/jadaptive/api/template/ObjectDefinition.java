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
		
		String resourceKey();
		
		String bundle() default "";
		
		ObjectType type() default ObjectType.COLLECTION;
		
		ObjectScope scope() default ObjectScope.GLOBAL;

		boolean hidden() default false;
		
		boolean system() default false;
		
		boolean recurse() default true;
		
		boolean creatable() default true;
		
		boolean updatable() default true;
		
		boolean deletable() default true;
		
		String[] aliases() default { };
		
		String defaultFilter() default "";
		
		String defaultColumn() default "";

		boolean requiresPermission() default true;
}
