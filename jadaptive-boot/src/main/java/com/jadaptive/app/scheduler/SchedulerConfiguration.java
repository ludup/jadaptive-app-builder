package com.jadaptive.app.scheduler;

import java.util.Objects;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Scheduler Configuration", resourceKey = SchedulerConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
public class SchedulerConfiguration extends SingletonUUIDEntity {

	public static final String RESOURCE_KEY = "schedulerConfig";
	
	@ObjectField(name = "Pool Size", 
			description = "The number of threads available to the scheduler",
			defaultValue = "10", 
			type = FieldType.INTEGER)
	Integer poolSize;

	public Integer getPoolSize() {
		return Objects.isNull(poolSize) ? 10 : poolSize;
	}

	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	protected final String getSingletonUuid() {
		return "fe63a4f9-ac4a-461d-b12a-ee76e8a6f1a8";
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
