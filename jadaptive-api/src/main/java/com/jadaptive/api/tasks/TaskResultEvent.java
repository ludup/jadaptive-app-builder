package com.jadaptive.api.tasks;

import com.jadaptive.api.events.UUIDEntityEvent;

public class TaskResultEvent extends UUIDEntityEvent<TaskResult> {

	private static final long serialVersionUID = 1765194672002156166L;

	public TaskResultEvent(TaskResult result) {
		super(result.getResourceKey(), result.getEventGroup(), result);
	}



}
