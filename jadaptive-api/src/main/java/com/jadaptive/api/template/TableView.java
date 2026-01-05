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
		
		/**
		 * Will renderings of this table include columns from any parent
		 * entity?
		 * 
		 * @return table includes columns parent entity
		 */
		boolean parentColumns() default true;

		
		/**
		 * Will renderings of this table include columns from any child
		 * entity?
		 * 
		 * @return table includes columns child entities
		 */
		boolean childColumns() default true;
		
		/**
		 * Will columns from this table be rendered when a parent table is
		 * rendered and has chosen to render child columns using {@link TableView#childColumns()} of <code>true</code>.
		 * 
		 * @return table columns will be rendered in a parent entity that renders child columns.
		 */
		boolean asChildColumns() default true;

		/**
		 * Will columns from this table be rendered when a parent table is
		 * rendered and has chosen to render parent columns using {@link TableView#parentColumns()} of <code>true</code>.
		 * 
		 * @return table columns will be rendered in a parent entity that renders parent columns.
		 */
		boolean asParentColumns() default true;
		
		
}
