package com.jadaptive.app.security;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Template;

@Template(name = "IP Restriction", resourceKey = IPRestriction.RESOURCE_KEY, scope = EntityScope.GLOBAL, type = EntityType.COLLECTION)
public class IPRestriction extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "ipRestriction";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
