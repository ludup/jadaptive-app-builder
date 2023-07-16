package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.JadaptiveIgnore;
import com.jadaptive.api.repository.NamedDocument;

@ObjectDefinition(resourceKey = ObjectTemplate.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, system = true)
@UniqueIndex(columns = {"resourceKey"})
@ObjectCache
public class ObjectTemplate extends TemplateUUIDEntity implements NamedDocument {

	private static final long serialVersionUID = -8159475909799827150L;

	public static final String RESOURCE_KEY = "objectTemplates";
	
	@ObjectField(type = FieldType.ENUM, defaultValue = "SYSTEM")
	ObjectTemplateType templateType;
	
	@ObjectField(type = FieldType.ENUM, defaultValue = "COLLECTION")
	ObjectType type;
	
	@ObjectField(type = FieldType.ENUM, defaultValue = "GLOBAL")
	ObjectScope scope;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String resourceKey;

	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String bundle;
	
	@ObjectField(type = FieldType.TEXT)
	String defaultFilter;
	
	@ObjectField(type = FieldType.TEXT)
	String defaultColumn;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = FieldTemplate.RESOURCE_KEY)
	Collection<FieldTemplate> fields = new ArrayList<>();
	
	Map<String,FieldTemplate> fieldsByName;
	String templateClass; 
	
	@ObjectField(type = FieldType.TEXT)
	Collection<String> aliases = new ArrayList<>();
	
	@ObjectField(type = FieldType.TEXT)
	String parentTemplate;

	@ObjectField(type = FieldType.TEXT)
	Collection<String> childTemplates = new TreeSet<>();
	
	@ObjectField(type = FieldType.BOOL, hidden = true)
	Boolean creatable;
	
	@ObjectField(type = FieldType.BOOL, hidden = true)
	Boolean updatable;
	
	@ObjectField(type = FieldType.BOOL, hidden = true)
	Boolean deletable;
	
	@ObjectField(type = FieldType.BOOL, hidden = true)
	Boolean permissionProtected;
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	String nameField;
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	String classDefinition;
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	String collectionKey;
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	String displayKey;
	
	@ObjectField(searchable = true, unique = true, type = FieldType.TEXT, nameField = true)
	@Validator(type = ValidationType.REQUIRED)
	protected String name;

	@ObjectField(type = FieldType.TEXT, hidden = true)
	Collection<String> extensions;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
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

	public String getEventGroup() {
		return RESOURCE_KEY;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}
	
	public String getCollectionKey() {
		return collectionKey;
	}
	
	public void setCollectionKey(String collectionKey) {
		this.collectionKey = collectionKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public ObjectTemplateType getTemplateType() {
		return templateType;
	}

	public void setTemplateType(ObjectTemplateType templateType) {
		this.templateType = templateType;
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

	public String getDefaultColumn() {
		if(StringUtils.isNotBlank(nameField)) {
			return nameField;
		}
		return StringUtils.isBlank(defaultColumn) ? "uuid" : defaultColumn;
	}

	public void setDefaultColumn(String defaultColumn) {
		this.defaultColumn = defaultColumn;
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
		return new HashSet<>(childTemplates);
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
		return permissionProtected && scope!=ObjectScope.PERSONAL;
	}

	public void setPermissionProtected(Boolean permissionProtected) {
		this.permissionProtected = permissionProtected;
	}
	
	@JadaptiveIgnore
	public String getCanonicalName() {
		return "com.jadaptive.dynamic." + StringUtils.capitalize(resourceKey);
	}

	public Boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(Boolean deletable) {
		this.deletable = deletable;
	}

	public boolean isSingleton() {
		return type == ObjectType.SINGLETON;
	}

	public String getClassDefinition() {
		return classDefinition;
	}

	public void setClassDefinition(String classDefinition) {
		this.classDefinition = classDefinition;
	}

	public String getDisplayKey() {
		if(isExtended()) {
			return displayKey;
		} else {
			return getResourceKey();
		}
	}

	public void setDisplayKey(String displayKey) {
		this.displayKey = displayKey;
	}

	public Collection<String> getExtensions() {
		return extensions;
	}

	public void setExtensions(Collection<String> extensions) {
		this.extensions = extensions;
	}

	public boolean isExtended() {
		return templateType==ObjectTemplateType.EXTENDED;
	}
}
