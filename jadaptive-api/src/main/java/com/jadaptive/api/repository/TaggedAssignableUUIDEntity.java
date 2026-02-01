package com.jadaptive.api.repository;

import java.util.Collection;
import java.util.HashSet;

import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

public abstract class TaggedAssignableUUIDEntity extends AssignableUUIDEntity implements NamedDocument {

	private static final long serialVersionUID = 2690511827179922811L;

	@ObjectField(searchable = true, unique = true, type = FieldType.TEXT, nameField = true, weight = 0)
	@Validator(type = ValidationType.REQUIRED)
	protected String name;

	@ObjectField(type = FieldType.TEXT, searchable = true, renderer = FieldRenderer.TAGS)
	private Collection<String> tags;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Collection<String> getTags() {
		return tags;
	}

	public void setTags(Collection<String> tags) {
		this.tags = new HashSet<>(tags);
	}
}
