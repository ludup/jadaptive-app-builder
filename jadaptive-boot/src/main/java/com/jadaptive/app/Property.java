package com.jadaptive.app;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = Property.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
public class Property extends UUIDEntity {

	private static final long serialVersionUID = -4021700778301136286L;

	public static final String RESOURCE_KEY = "properties";
	
	@ObjectField(type=FieldType.TEXT, unique = true)
	String key;
	
	@ObjectField(type=FieldType.TEXT_AREA)
	String value;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	

}
