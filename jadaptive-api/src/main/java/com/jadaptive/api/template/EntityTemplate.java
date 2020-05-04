package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.NamedUUIDEntity;

@Template(name = "Template", resourceKey = "entityTemplate", scope = EntityScope.GLOBAL, type = EntityType.COLLECTION)
@UniqueIndex(columns = {"resourceKey"})
public class EntityTemplate extends NamedUUIDEntity {

	EntityType type;
	EntityScope scope;
	String resourceKey;
	String defaultFilter;
	Collection<FieldTemplate> fields = new ArrayList<>();
	Map<String,FieldTemplate> fieldsByName;
	String templateClass; 
	Collection<String> aliases = new ArrayList<>();
	String parentTemplate;
	
	public EntityTemplate() {
		
	}
	
	public EntityScope getScope() {
		return scope;
	}

	public void setScope(EntityScope scope) {
		this.scope = scope;
	}
	
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

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public Collection<String> getAliases() {
		return aliases;
	}

	public void setAliases(Collection<String> aliases) {
		this.aliases = aliases;
	}

	public String getDefaultFilter() {
		return defaultFilter;
	}

	public void setDefaultFilter(String defaultFilter) {
		this.defaultFilter = defaultFilter;
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

	public void setTemplateClass(String templateClass) {
		this.templateClass = templateClass;
	}
	
	public String getTemplateClass() {
		return templateClass;
	}

	public String getParentTemplate() {
		return parentTemplate;
	}

	public void setParentTemplate(String parentTemplate) {
		this.parentTemplate = parentTemplate;
	}
}
