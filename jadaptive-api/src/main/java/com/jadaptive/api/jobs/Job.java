package com.jadaptive.api.jobs;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.tasks.Task;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Job", resourceKey = Job.RESOURCE_KEY, type = EntityType.COLLECTION)
public class Job extends NamedUUIDEntity {

	public static final String RESOURCE_KEY = "job";
	
	@Column(name = "Task", 
			description = "The task to execute",
			type = FieldType.OBJECT_EMBEDDED)
	Task task;
	
	@Column(name = "Job ID", 
			description = "The unique identifier for this job",
			type = FieldType.OBJECT_EMBEDDED,
			unique = true,
			searchable = true)
	Integer shortId;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Integer getShortId() {
		return shortId;
	}

	public void setShortId(Integer shortId) {
		this.shortId = shortId;
	}
}
