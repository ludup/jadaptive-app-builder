package com.jadaptive.entity.repository;

import java.util.Map;

public class EntityImpl extends AbstractUUIDEntityImpl implements Entity {

	String templateUUID;

	Map<String,String> properties;
	
	public String getTemplateUUID() {
		return templateUUID;
	}

	public void setTemplateUUID(String templateUUID) {
		this.templateUUID = templateUUID;
	}

//	public void generateMap(Map<String, String> properties) throws ParseException {
//		super.generateMap(properties);
//		properties.putAll(this.properties);
//	}
//
//	public void fromMap(String uuid, Map<String, String> properties) throws ParseException {
//		super.fromMap(uuid, properties);
//		this.properties = new HashMap<>(properties);
//	}

	public void setValue(String key, String value) {
		properties.put(key,  value);
	}

	public String getValue(FieldTemplate t) {
		
		String key = String.format("%s.%s", getUuid(), t.getResourceKey());
		return properties.get(key);
	}
	
}
