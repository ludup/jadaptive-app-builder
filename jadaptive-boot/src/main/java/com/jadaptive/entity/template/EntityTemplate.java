package com.jadaptive.entity.template;

import java.util.Set;

import com.jadaptive.entity.EntityType;
import com.jadaptive.repository.NamedUUIDEntity;

public class EntityTemplate extends NamedUUIDEntity {

	EntityType type;
	Set<FieldTemplate> fields;
	
	public EntityType getType() {
		return type;
	}
	
	public void setType(EntityType type) {
		this.type = type;
	}

	public Set<FieldTemplate> getFields() {
		return fields;
	}

	public void setFields(Set<FieldTemplate> fields) {
		this.fields = fields;
	}
}
