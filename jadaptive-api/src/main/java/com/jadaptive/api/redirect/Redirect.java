package com.jadaptive.api.redirect;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey =  Redirect.RESOURCE_KEY)
public class Redirect extends UUIDEntity {

	private static final long serialVersionUID = 3127030622719884860L;

	public static final String RESOURCE_KEY = "redirects";
	
	@ObjectField(required = true, searchable = true, type = FieldType.TEXT)
	String path;
	
	@ObjectField(required = true, type = FieldType.TEXT)
	String location;
	
	
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
