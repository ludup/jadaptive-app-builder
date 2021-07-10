package com.jadaptive.app.tenant;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.session.SessionConfiguration;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = SessionConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
public class SystemConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = 9123803748701702401L;
	
	public static final String RESOURCE_KEY = "systemConfiguration";
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	Boolean setupComplete;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public Boolean getSetupComplete() {
		return setupComplete==null ? Boolean.FALSE : setupComplete;
	}

	public void setSetupComplete(Boolean setupComplete) {
		this.setupComplete = setupComplete;
	}
	
	

}
