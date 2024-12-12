package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableView {
				
		String[] defaultColumns();
		
		DynamicColumn[] otherColumns() default { };
		
		boolean requiresView() default true;
		
		boolean requiresUpdate() default false;
		
		boolean requiresCreate() default false;
		
		SortOrder sortOrder() default SortOrder.ASC;

		String sortField() default "_id";
		
		boolean multipleDelete() default false;
		
		
}
