package com.jadaptive.app.scheculer;

import java.util.Objects;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Scheduler Configuration", resourceKey = "schedulerConfig", type = EntityType.SINGLETON)
public class SchedulerConfiguration extends SingletonUUIDEntity {

	@Column(name = "Pool Size", 
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
	
	
}
