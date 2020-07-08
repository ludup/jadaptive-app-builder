package com.jadaptive.api.tasks;

import java.util.ArrayList;
import java.util.List;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = Trigger.RESOURCE_KEY, type = ObjectType.OBJECT)
public class Trigger extends UUIDEntity {

	private static final long serialVersionUID = -8047595311868714763L;

	public static final String RESOURCE_KEY = "trigger";
	
	@ObjectField(type = FieldType.ENUM)
	TriggerType type;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	List<TriggerMapping> taskMappings = new ArrayList<>();

	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	List<TriggerMapping> globalMappings = new ArrayList<>();
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	Task task;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
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
