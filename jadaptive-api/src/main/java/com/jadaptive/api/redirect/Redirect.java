package com.jadaptive.api.redirect;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey =  Redirect.RESOURCE_KEY, bundle = Redirect.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
@TableView(defaultColumns = { "hostname", "path", "location" })
public class Redirect extends AbstractUUIDEntity {

	private static final long serialVersionUID = 3127030622719884860L;

	public static final String RESOURCE_KEY = "redirects";
	
	@ObjectField(searchable = true, type = FieldType.TEXT)
	String hostname;
	
	@ObjectField(searchable = true, type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String path;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String location;
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}
