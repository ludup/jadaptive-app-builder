package com.jadaptive.app;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(name = "Properties", resourceKey = Property.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
public class Property extends UUIDEntity {

	public static final String RESOURCE_KEY = "properties";
	
	@ObjectField(name = "Key", description = "The unique key of this property", type=FieldType.TEXT, unique = true)
	String key;
	
	@ObjectField(name = "Value", description = "The value of this property", type=FieldType.TEXT_AREA)
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