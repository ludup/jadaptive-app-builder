package com.jadaptive.entity.template;

import java.util.ArrayList;
import java.util.Collection;

import com.jadaptive.entity.EntityType;
import com.jadaptive.repository.NamedUUIDEntity;

public class EntityTemplate extends NamedUUIDEntity {

	EntityType type;
	Collection<FieldTemplate> fields = new ArrayList<>();
	
	public EntityType getType() {
		return type;
	}
	
	public void setType(EntityType type) {
		this.type = type;
	}

	public Collection<FieldTemplate> getFields() {
		return fields;
	}

	public void setFields(Collection<FieldTemplate> fields) {
		this.fields = fields;
	}
}
