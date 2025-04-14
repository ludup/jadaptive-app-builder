package com.jadaptive.api.tenant;

import java.util.Collection;
import java.util.HashSet;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldOptions;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = DatabaseConnection.RESOURCE_KEY, 
	scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, 
	system = true, creatable = true, defaultColumn = "name")
@GenerateEventTemplates(value = DatabaseConnection.RESOURCE_KEY)
@TableView(defaultColumns = { "name"})
public class DatabaseConnection extends NamedUUIDEntity implements NamedDocument {

	private static final long serialVersionUID = 1567817173441528990L;

	public static final String RESOURCE_KEY = "databases";
	
	@ObjectField(type = FieldType.TEXT,  options = FieldOptions.MANUAL_ENCRYPTION)
	String connectionString;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.HOSTNAME)
	Collection<String> nodes;
	
	@ObjectField(type = FieldType.COUNTRY)
	Collection<String> automaticMapping = new HashSet<>();

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	public Collection<String> getAutomaticMapping() {
		return automaticMapping;
	}

	public void setAutomaticMapping(Collection<String> automaticMapping) {
		this.automaticMapping = automaticMapping;
	}

	public Collection<String> getNodes() {
		return nodes;
	}

	public void setNodes(Collection<String> nodes) {
		this.nodes = nodes;
	}
}
