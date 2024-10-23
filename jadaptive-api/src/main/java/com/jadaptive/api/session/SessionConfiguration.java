package com.jadaptive.api.session;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = SessionConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
public class SessionConfiguration extends SingletonUUIDEntity {
	
	private static final long serialVersionUID = 6441953663902277562L;

	public static final String RESOURCE_KEY = "sessionConfiguration";

	@ObjectField(type = FieldType.INTEGER, defaultValue = "60")
	@Validator(type = ValidationType.RANGE, value = "1-1440")
	private int timeout;

	@ObjectField(type = FieldType.BOOL, defaultValue = "true")
	private boolean enableCsrf = true;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public boolean getEnableCsrf() {
		return enableCsrf;
	}

	public void setEnableCsrf(boolean enableCsrf) {
		this.enableCsrf = enableCsrf;
	}

}