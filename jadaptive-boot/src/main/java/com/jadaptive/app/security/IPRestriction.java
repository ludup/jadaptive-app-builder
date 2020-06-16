package com.jadaptive.app.security;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(name = "IP Restriction", resourceKey = IPRestriction.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
public class IPRestriction extends AbstractUUIDEntity {

	private static final long serialVersionUID = -4455075053450624115L;
	
	public static final String RESOURCE_KEY = "ipRestriction";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
