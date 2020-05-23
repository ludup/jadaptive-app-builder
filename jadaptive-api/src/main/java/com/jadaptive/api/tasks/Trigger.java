package com.jadaptive.api.tasks;

import java.util.ArrayList;
import java.util.List;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Trigger", resourceKey = Trigger.RESOURCE_KEY, type = ObjectType.OBJECT)
public class Trigger extends UUIDEntity {

	public static final String RESOURCE_KEY = "trigger";
	
	@Column(name = "Trigger Type", description = "When to trigger the tasks", type = FieldType.ENUM)
	TriggerType type;
	
	@Column(name = "Task Mappings", description = "A set of variables that map parameters from the previous task to the executing task", type = FieldType.OBJECT_EMBEDDED)
	List<TriggerMapping> taskMappings = new ArrayList<>();

	@Column(name = "Global Mappings", description = "Variables that should be stored for future use future tasks", type = FieldType.OBJECT_EMBEDDED)
	List<TriggerMapping> globalMappings = new ArrayList<>();
	
	@Column(name = "Task", description = "The task to execute", type = FieldType.OBJECT_EMBEDDED)
	Task task;
	
	@Column(name = "Triggers", 
			description = "Additional tasks to conditionally execute",
			type = FieldType.OBJECT_EMBEDDED)
	List<Trigger> triggers = new ArrayList<>();
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public TriggerType getType() {
		return type;
	}

	public void setType(TriggerType tyoe) {
		this.type = tyoe;
	}

	public List<TriggerMapping> getTaskMappings() {
		return taskMappings;
	}

	public void setTaskMappings(List<TriggerMapping> taskMappings) {
		this.taskMappings = taskMappings;
	}

	public List<TriggerMapping> getGlobalMappings() {
		return globalMappings;
	}

	public void setGlobalMappings(List<TriggerMapping> globalMappings) {
		this.globalMappings = globalMappings;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<Trigger> triggers) {
		this.triggers = triggers;
	}
}
