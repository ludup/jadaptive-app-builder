package com.jadaptive.api.scheduler;

import java.util.Date;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.jobs.Job;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Cron Schedule", resourceKey = CronSchedule.RESOURCE_KEY, type = EntityType.COLLECTION)
public class CronSchedule extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "cronSchedules";
	
	@Column(name = "Cron Expression", 
			description = "The crontab expression for this schedule",
			type = FieldType.TEXT)
	String expression;

	@Column(name = "Job", 
			description = "The job to execute",
			type = FieldType.OBJECT_REFERENCE)
	Job job;
	
	@Column(name = "Disabled", 
			description = "Set this flag to disable execution of this job",
			type = FieldType.BOOL)
	Boolean disabled;
	
	@Column(name = "Last Execution Finished", 
			description = "The timestamp when the last execution finished",
			type = FieldType.TIMESTAMP)
	Date lastExecutionFinished;
	
	@Column(name = "Last Execution Started", 
			description = "The timestamp when the last execution started",
			type = FieldType.TIMESTAMP)
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

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

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
