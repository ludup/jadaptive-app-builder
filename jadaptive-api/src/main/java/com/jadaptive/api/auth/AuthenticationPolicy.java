package com.jadaptive.api.auth;

import java.util.Collection;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = AuthenticationPolicy.RESOURCE_KEY, scope = ObjectScope.GLOBAL, defaultColumn = "name")
@ObjectViews({ @ObjectViewDefinition(bundle = AuthenticationPolicy.RESOURCE_KEY, value = "factors", weight = -9999)})
@TableView(defaultColumns = "name", requiresCreate = false, requiresUpdate = true)
public class AuthenticationPolicy extends AssignableUUIDEntity {

	private static final long serialVersionUID = -4581883248747380399L;

	public static final String RESOURCE_KEY = "authenticationPolicy";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@ObjectField(searchable = true, unique = true, type = FieldType.TEXT, nameField = true)
	String name;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = AuthenticationModule.RESOURCE_KEY)
	@ObjectView(value = "factors")
	Collection<String> requiredAuthenticators;

	@ObjectField(type = FieldType.INTEGER)	
	Integer weight;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<String> getRequiredAuthenticators() {
		return requiredAuthenticators;
	}

	public void setRequiredAuthenticators(Collection<String> requiredAuthenticators) {
		this.requiredAuthenticators = requiredAuthenticators;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	
}
