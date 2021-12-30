package com.jadaptive.api.tasks;

import com.jadaptive.api.events.SystemEvent;

public class TaskResultEvent extends SystemEvent<TaskResult> {

	private static final long serialVersionUID = 1765194672002156166L;

	public TaskResultEvent(String resourceKey, String group) {
		super(resourceKey, group);
	}

	public TaskResultEvent(TaskResult result) {
		super(result.getResourceKey(), result.getEventGroup());
	}



}
