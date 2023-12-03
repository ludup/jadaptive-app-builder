package com.jadaptive.api.tasks;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.CustomEvent;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(recurse = false, resourceKey = "taskResult", type = ObjectType.OBJECT)
public abstract class TaskResult extends CustomEvent {

	private static final long serialVersionUID = 5269923561715448655L;

	public TaskResult(String resourceKey, Throwable e) {
		super(resourceKey, e);
	}

	public TaskResult(String resourceKey) {
		super(resourceKey);
	}
	
	public abstract String getEventGroup();
}
