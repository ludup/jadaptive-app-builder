package com.jadaptive.api.scheduler;

import java.util.Date;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = CronSchedule.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class CronSchedule extends AbstractUUIDEntity {

	private static final long serialVersionUID = -3022610943013191464L;

	public static final String RESOURCE_KEY = "cronSchedules";
	
	@ObjectField(type = FieldType.TEXT)
	String expression;

//	@ObjectField(type = FieldType.OBJECT_REFERENCE)
//	Job job;
	
	@ObjectField(type = FieldType.BOOL)
	Boolean disabled;
	
	@ObjectField(type = FieldType.TIMESTAMP)
	Date lastExecutionFinished;
	
	@ObjectField(type = FieldType.TIMESTAMP)
	Date lastExecutionStarted;
	
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

//	public Job getJob() {
//		return job;
//	}

//	public void setJob(Job job) {
//		this.job = job;
//	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Date getLastExecutionFinished() {
		return lastExecutionFinished;
	}

	public void setLastExecutionFinished(Date lastExecutionFinished) {
		this.lastExecutionFinished = lastExecutionFinished;
	}

	public Date getLastExecutionStarted() {
		return lastExecutionStarted;
	}

	public void setLastExecutionStarted(Date lastExecutionStarted) {
		this.lastExecutionStarted = lastExecutionStarted;
	}
}
