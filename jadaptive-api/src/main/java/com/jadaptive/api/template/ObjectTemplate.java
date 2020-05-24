package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;

@Template(name = "Template", resourceKey = ObjectTemplate.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
@UniqueIndex(columns = {"resourceKey"})
public class ObjectTemplate extends NamedUUIDEntity {

	public static final String RESOURCE_KEY = "objectTemplates";
	
	ObjectType type;
	ObjectScope scope;
	String resourceKey;
	String defaultFilter;
	Collection<FieldDefinition> fields = new ArrayList<>();
	Map<String,FieldDefinition> fieldsByName;
	String templateClass; 
	Collection<String> aliases = new ArrayList<>();
	String parentTemplate;
	Collection<String> defaultColumns = new ArrayList<>();
	Collection<String> optionalColumns = new ArrayList<>();
	
	public ObjectTemplate() {
		
	}
	
	public ObjectScope getScope() {
		return scope;
	}

	public void setScope(ObjectScope scope) {
		this.scope = scope;
	}
	
	public ObjectType getType() {
		return type;
	}
	
	public void setType(ObjectType type) {
		this.type = type;
	}

	public Collection<FieldDefinition> getFields() {
		return fields;
	}

	public void setFields(Collection<FieldDefinition> fields) {
		this.fields = fields;
	}

	public FieldDefinition getField(String name) {
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

	@JsonIgnore
	public Map<String,FieldDefinition> toMap() {
		
		if(Objects.isNull(fieldsByName)) {
			Map<String,FieldDefinition> tmp = new HashMap<>();
			for(FieldDefinition t : fields) {
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

	public Collection<String> getDefaultColumns() {
		return defaultColumns;
	}

	public void setDefaultColumns(Collection<String> defaultColumns) {
		this.defaultColumns = new ArrayList<>(defaultColumns);
	}

	public Collection<String> getOptionalColumns() {
		return optionalColumns;
	}

	public void setOptionalColumns(Collection<String> optionalColumns) {
		this.optionalColumns = new ArrayList<>(optionalColumns);
	}
	
}
