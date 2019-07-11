package com.jadaptive.entity;

import java.text.ParseException;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jadaptive.entity.template.FieldTemplate;
import com.jadaptive.repository.AbstractUUIDEntity;

@JsonSerialize(using=EntitySerializer.class)
@JsonDeserialize(using=EntityDeserializer.class)
public class Entity extends AbstractUUIDEntity {

	String resourceKey;
	Map<String,Map<String,String>> properties;
	
	public Entity() {	
	}
	
	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getValue(String fieldName) {
		String value =  checkExistsOrDefault(getUuid(), properties, fieldName, null);
		if(Objects.isNull(value)) {
			throw new IllegalArgumentException(String.format("%s is not a valid field for entity %s", fieldName, getResourceKey()));
		}
		return value;
	}
	
	public String getValue(FieldTemplate t) {
		return checkExistsOrDefault(getUuid(), properties, t.getResourceKey(), t.getDefaultValue());
	}

	@Override
	public void store(Map<String, Map<String, String>> properties) throws ParseException {
		
		super.store(properties);
		properties.putAll(this.properties);
	}

	@Override
	public void load(String uuid, Map<String, Map<String, String>> properties) throws ParseException {
		
		super.load(uuid, properties);
		this.properties = properties;
	}

	private String checkExistsOrDefault(String uuid, Map<String, Map<String, String>> properties, String field, String defaultValue) {
		Map<String,String> objectProperties = properties.get(uuid);
		if(Objects.isNull(objectProperties)) {
			throw new IllegalStateException(String.format("No properties for uuid",uuid));
		}
		
		String value = objectProperties.get(field);
		
		if(Objects.isNull(value)) {
			return defaultValue;
		}
		
		return value;
	}
}
