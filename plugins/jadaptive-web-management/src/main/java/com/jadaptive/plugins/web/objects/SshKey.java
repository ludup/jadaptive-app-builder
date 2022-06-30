package com.jadaptive.plugins.web.objects;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.plugins.web.ui.tenant.TenantWizard;

@ObjectDefinition(resourceKey = SshKey.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT, bundle = TenantWizard.RESOURCE_KEY)
public class SshKey extends AbstractUUIDEntity {

	private static final long serialVersionUID = 9164598117525455668L;

	public static final String RESOURCE_KEY = "sshKey";
	
	@ObjectField(type = FieldType.TEXT_AREA)
	String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
