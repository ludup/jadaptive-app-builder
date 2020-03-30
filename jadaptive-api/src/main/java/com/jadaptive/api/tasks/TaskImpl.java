package com.jadaptive.api.tasks;

import org.pf4j.ExtensionPoint;

public interface TaskImpl<T extends Task> extends ExtensionPoint {

	String getResourceKey();
	
	TaskResult doTask(T task);
}
