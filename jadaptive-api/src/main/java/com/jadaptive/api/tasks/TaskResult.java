package com.jadaptive.api.tasks;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.events.CustomEvent;
import com.jadaptive.api.template.Template;

@Template(name = "Task Result", recurse = false, resourceKey = "taskResult", type = EntityType.OBJECT)
public class TaskResult extends CustomEvent {

	public TaskResult(String resourceKey, Throwable e) {
		super(resourceKey, e);
	}

	public TaskResult(String resourceKey) {
		super(resourceKey);
	}
}
