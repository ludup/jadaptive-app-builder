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

@ObjectDefinition(resourceKey = ObjectTemplate.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
@UniqueIndex(columns = {"resourceKey"})
public class ObjectTemplate extends NamedUUIDEntity {

	private static final long serialVersionUID = -8159475909799827150L;

	public static final String RESOURCE_KEY = "objectTemplates";
	
	@ObjectField(name = "Type", description = "The type of this object",
			type = FieldType.ENUM, defaultValue = "COLLECTION")
	ObjectType type;
	
	@ObjectField(name = "Scope", description = "The scope of this object",
			type = FieldType.ENUM, defaultValue = "GLOBAL")
	ObjectScope scope;
	
	@ObjectField(name = "Resource Key", description = "The identifier for this template",
			type = FieldType.TEXT, required = true)
	String resourceKey;
	
	@ObjectField(name = "Default Filter", description = "A filter to apply to default queries",
			type = FieldType.TEXT)
	String defaultFilter;
	
	@ObjectField(name = "Fields", description = "The fields of this template",
			type = FieldType.OBJECT_EMBEDDED, references = FieldTemplate.RESOURCE_KEY)
	Collection<FieldTemplate> fields = new ArrayList<>();
	
	Map<String,FieldTemplate> fieldsByName;
	String templateClass; 
	
	@ObjectField(name = "Aliases", description = "The aliases of this template",
			type = FieldType.TEXT)
	Collection<String> aliases = new ArrayList<>();
	
	@ObjectField(name = "Parent", description = "The parent of this template",
			type = FieldType.TEXT)
	String parentTemplate;

	
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

	@JsonIgnore
	public Map<String,FieldTemplate> toMap() {
		
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
