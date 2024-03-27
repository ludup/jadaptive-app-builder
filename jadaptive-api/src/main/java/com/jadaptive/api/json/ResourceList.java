package com.jadaptive.api.json;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value=Include.NON_NULL)
public class ResourceList<T> extends ResourceStatus<T> {

	private static final long serialVersionUID = -170667128130340102L;

	public static final int DEFAULT_MAXIMUM_RESOURCES = 1000;
	
	private Collection<T> resources;
	private Map<String,String> properties;
	
	public ResourceList() {
	}
	
	public ResourceList(Collection<T> resources) {
		this.resources = resources;
	}
	
	public ResourceList(boolean success, String message) {
		super(success, message);
	}
	
	public ResourceList(Map<String,String> properties, Collection<T> resources) {
		this.properties = properties;
		this.resources = resources;
	}
	
	public Collection<T> getResources() {
		return resources;
	}
	
	public Map<String,String> getProperties() {
		return properties;
	}
}

