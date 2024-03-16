package com.jadaptive.api.quotas;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = QuotaKey.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class QuotaKey extends NamedUUIDEntity {

	private static final long serialVersionUID = -1548369187702913896L;
	public static final String RESOURCE_KEY = "quotaKey";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}
