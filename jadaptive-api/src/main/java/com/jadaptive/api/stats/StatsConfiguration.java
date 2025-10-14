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
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "00", weight = 0)
	private int daysToRetain;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "28", weight = 10)
	private int evaluatorQueryDays;

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

	public int getEvaluatorQueryDays() {
		return evaluatorQueryDays;
	}

	public void setEvaluatorQueryDays(int evaluatorQueryDays) {
		this.evaluatorQueryDays = evaluatorQueryDays;
	}



}
