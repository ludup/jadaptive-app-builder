package com.jadaptive.app.scheduler;

import java.util.Objects;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.ui.pages.config.ConfigurationItem;

@ObjectDefinition(resourceKey = SchedulerConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
@ConfigurationItem(icon = "fa-calendar-clock", path = "/app/ui/config/schedulerConfig", resourceKey = SchedulerConfiguration.RESOURCE_KEY, system = true)
public class SchedulerConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = 3875690639044948654L;

	public static final String RESOURCE_KEY = "schedulerConfig";
	
	@ObjectField(defaultValue = "10", 
			type = FieldType.INTEGER)
	Integer poolSize;

	public Integer getPoolSize() {
		return Objects.isNull(poolSize) ? 10 : poolSize;
	}

	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
