package com.jadaptive.api.tasks;

import com.jadaptive.api.events.ObjectEvent;

public abstract class TaskResultEvent<T extends TaskResult> extends ObjectEvent<T> {

	private static final long serialVersionUID = 1765194672002156166L;

	public TaskResultEvent(TaskResult result) {
		super(result.getResourceKey(), result.getEventGroup());
	}

	

}
