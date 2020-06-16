package com.jadaptive.api.session;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(name = "Session Configuration", resourceKey = SessionConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
public class SessionConfiguration extends SingletonUUIDEntity {
	
	private static final long serialVersionUID = 6441953663902277562L;

	public static final String RESOURCE_KEY = "sessionConfiguration";

	@ObjectField(name = "Timeout", 
			description = "The number of minutes of idle time before a session times out",
			type = FieldType.INTEGER, 
			defaultValue = "15")
	Integer timeout;

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public Integer getTimeout() {
		return timeout;
	}


	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}





}