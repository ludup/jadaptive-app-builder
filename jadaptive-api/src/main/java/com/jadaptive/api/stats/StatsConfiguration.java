package com.jadaptive.api.stats;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.ui.pages.config.ConfigurationItem;

@SuppressWarnings("serial")
@ObjectDefinition(bundle = StatsConfiguration.RESOURCE_KEY, resourceKey = StatsConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON, system = true)
@ConfigurationItem(icon = "fa-chart-simple", system = true)
@GenerateEventTemplates
public class StatsConfiguration extends SingletonUUIDEntity {

	public static final String RESOURCE_KEY = "statsConfiguration";
	
	/**
	 * Default to retain everything. This can be changed per-deployment.
	 * DO NOT CHANGE THIS DEFAULT FROM ZERO IN AS IT WOULD DELETE DATA FROM LIVE DEPLOYMENTS
	 */
	@ObjectField(type = FieldType.INTEGER, defaultValue = "0", weight = 0)
	private int daysToRetain;

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public int getDaysToRetain() {
		return daysToRetain;
	}

	public void setDaysToRetain(int daysToRetain) {
		this.daysToRetain = daysToRetain;
	}

}
