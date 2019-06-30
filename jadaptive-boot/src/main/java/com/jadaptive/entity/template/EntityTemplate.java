package com.jadaptive.entity.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.entity.EntityType;
import com.jadaptive.repository.NamedUUIDEntity;

public class EntityTemplate extends NamedUUIDEntity {

	EntityType type;
	Set<FieldTemplate> fields;
	Set<FieldCategory> categories;
	
	public EntityType getType() {
		return type;
	}
	
	public void setType(EntityType type) {
		this.type = type;
	}
	
	public Set<FieldCategory> getCategories() {
		return categories;
	}
	
	@JsonIgnore
	public Map<String,FieldCategory> getCategoriesMap() {
		Map<String,FieldCategory> tmp = new HashMap<>();
		for(FieldCategory c : categories) {
			tmp.put(c.getResourceKey(), c);
		}
		return tmp;
	}
	
	public void setCategories(Set<FieldCategory> categories) {
		this.categories = categories;
	}

	public Set<FieldTemplate> getFields() {
		return fields;
	}

	public void setFields(Set<FieldTemplate> fields) {
		this.fields = fields;
	}
}
