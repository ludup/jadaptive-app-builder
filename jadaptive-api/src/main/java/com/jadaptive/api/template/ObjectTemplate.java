package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.JadaptiveIgnore;
import com.jadaptive.api.repository.NamedUUIDEntity;

@ObjectDefinition(resourceKey = ObjectTemplate.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
@UniqueIndex(columns = {"resourceKey"})
public class ObjectTemplate extends NamedUUIDEntity {

	private static final long serialVersionUID = -8159475909799827150L;

	public static final String RESOURCE_KEY = "objectTemplates";
	
	@ObjectField(type = FieldType.ENUM, defaultValue = "COLLECTION")
	ObjectType type;
	
	@ObjectField(type = FieldType.ENUM, defaultValue = "GLOBAL")
	ObjectScope scope;
	
	@ObjectField(type = FieldType.TEXT, required = true)
	String resourceKey;

	@ObjectField(type = FieldType.TEXT, required = true)
	String bundle;
	
	@ObjectField(type = FieldType.TEXT)
	String defaultFilter;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = FieldTemplate.RESOURCE_KEY)
	Collection<FieldTemplate> fields = new ArrayList<>();
	
	Map<String,FieldTemplate> fieldsByName;
	String templateClass; 
	
	@ObjectField(type = FieldType.TEXT)
	Collection<String> aliases = new ArrayList<>();
	
	@ObjectField(type = FieldType.TEXT)
	String parentTemplate;

	@ObjectField(type = FieldType.TEXT)
	Collection<String> childTemplates = new ArrayList<>();
	
	@ObjectField(type = FieldType.HIDDEN)
	Boolean creatable;
	
	@ObjectField(type = FieldType.HIDDEN)
	Boolean updatable;
	
	@ObjectField(type = FieldType.HIDDEN)
	Boolean permissionProtected;
	
	@ObjectField(type = FieldType.HIDDEN)
	String nameField;
	
	public ObjectTemplate() {
		
	}

	public String getBundle() {
		return bundle;
	}
	
	public void setBundle(String bundle) {
		this.bundle = bundle;
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
	
	public String getCollectionKey() {
		String parentKey = getParentTemplate();
		if(StringUtils.isNotBlank(parentKey)) {
			return parentKey;
		}
		return getResourceKey();
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
	
	public void addChildTemplate(String childTemplate) {
		childTemplates.add(childTemplate);
	}
	
	public void setChildTemplates(Collection<String> childTemplates) {
		this.childTemplates = childTemplates;
	}
	
	public Collection<String> getChildTemplates() {
		return childTemplates;
	}

	public boolean hasParent() {
		return StringUtils.isNotBlank(getParentTemplate());
	}

	public Boolean isCreatable() {
		return creatable;
	}

	public void setCreatable(Boolean creatable) {
		this.creatable = creatable;
	}

	public Boolean isUpdatable() {
		return updatable;
	}

	public void setUpdatable(Boolean updatable) {
		this.updatable = updatable;
	}

	public String getNameField() {
		return nameField;
	}

	public void setNameField(String nameField) {
		this.nameField = nameField;
	}

	public Boolean getPermissionProtected() {
		return permissionProtected;
	}

	public void setPermissionProtected(Boolean permissionProtected) {
		this.permissionProtected = permissionProtected;
	}
	
	@JadaptiveIgnore
	public String getCanonicalName() {
		return "com.jadaptive.dynamic." + StringUtils.capitalize(resourceKey);
	}
}
