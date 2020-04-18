package com.jadaptive.api.jobs;

import java.util.ArrayList;
import java.util.List;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.tasks.Task;
import com.jadaptive.api.tasks.Trigger;
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
	
	@Column(name = "Triggers", 
			description = "Additional tasks to conditionally execute",
			type = FieldType.OBJECT_EMBEDDED)
	List<Trigger> triggers = new ArrayList<>();
	
	@Column(name = "Job ID", 
			description = "The unique identifier for this job",
			type = FieldType.OBJECT_EMBEDDED,
			unique = true,
			searchable = true)
	Integer shortId;

	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
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

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<Trigger> triggers) {
		this.triggers = triggers;
	}
}
