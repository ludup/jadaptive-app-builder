package com.jadaptive.api.tasks;

import org.pf4j.ExtensionPoint;

public interface TaskImpl<T extends Task> extends ExtensionPoint {

	String getResourceKey();
	
	TaskResult doTask(T task, String executionId);

	@SuppressWarnings("unchecked")
	default TaskResult executeTask(Task task, String executionId) { return doTask((T) task, executionId); }
	
	default String getIconGroup() { return "fa-solid"; }
	
	String getIcon();

	String getBundle();
}
