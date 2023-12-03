package com.jadaptive.api.template;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jadaptive.api.tasks.TaskImpl;
import com.jadaptive.api.tasks.TaskResult;

@Retention(RUNTIME)
@Target(TYPE)
public @interface TaskDefinition {

	Class<? extends TaskImpl<?>> impl();
	
	Class<? extends TaskResult> result();

	String bundle();
}
