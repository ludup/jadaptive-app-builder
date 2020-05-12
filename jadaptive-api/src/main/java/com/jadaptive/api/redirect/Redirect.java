package com.jadaptive.api.redirect;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Redirects", resourceKey =  Redirect.RESOURCE_KEY)
public class Redirect extends UUIDEntity {

	public static final String RESOURCE_KEY = "redirects";
	
	@Column(name = "Path", description = "The path to redirect", required = true, searchable = true, type = FieldType.TEXT)
	String path;
	
	@Column(name = "Location", description = "The location to redirect to", required = true, type = FieldType.TEXT)
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
