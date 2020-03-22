package com.jadaptive.api.scheculer;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Scheduled Job", resourceKey = "scheduledJobs", type = EntityType.COLLECTION)
public class ScheduledJob extends NamedUUIDEntity {

	@Column(name = "Execution Context",
			description = "The context of where this job is executed",
			type = FieldType.ENUM)
	ExecutionContext executionContext;
	
	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}
	
	
	
}
