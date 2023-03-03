package com.jadaptive.api.json;

import java.util.Collection;

import com.jadaptive.api.template.ObjectTemplate;

public class EntityTableStatus<T> extends TableStatus<T> {

	ObjectTemplate template;
	
	public EntityTableStatus(boolean success, String message) {
		super(success, message);
	}
	
	public EntityTableStatus(ObjectTemplate template, Collection<T> result, long total) {
		super(result, total);
		this.template = template;
	}

	public ObjectTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ObjectTemplate template) {
		this.template = template;
	}

}
