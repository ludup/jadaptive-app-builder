package com.jadaptive.api.ip;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = IPStackConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON, system = true)
@ObjectViews({@ObjectViewDefinition(value = IPStackConfiguration.API_VIEW, bundle = IPStackConfiguration.RESOURCE_KEY)})
public class IPStackConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = -4626299154896501190L;

	public static final String RESOURCE_KEY = "ipStackConfig";
	
	public static final String API_VIEW = "api";
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(API_VIEW)
	String apiKey;
	
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}
