package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.app.repository.NamedUUIDEntity;

public class EntityTemplate extends NamedUUIDEntity {

	EntityType type;
	Collection<FieldTemplate> fields = new ArrayList<>();
	Map<String,FieldTemplate> fieldsByName;
	
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

	public FieldTemplate getField(String name) {
		return toMap().get(name);
	}
	
	Map<String,FieldTemplate> toMap() {
		
		if(Objects.isNull(fieldsByName)) {
			Map<String,FieldTemplate> tmp = new HashMap<>();
			for(FieldTemplate t : fields) {
				tmp.put(t.getResourceKey(), t);
			}
			fieldsByName = tmp;
		}
		return fieldsByName;
	}
}
