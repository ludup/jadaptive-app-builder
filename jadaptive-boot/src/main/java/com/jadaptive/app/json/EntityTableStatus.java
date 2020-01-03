package com.jadaptive.app.json;

import java.util.Collection;

import com.jadaptive.api.template.EntityTemplate;

public class EntityTableStatus<T> extends TableStatus<T> {

	EntityTemplate template;
	
	public EntityTableStatus(boolean success, String message) {
		super(success, message);
	}
	
	public EntityTableStatus(EntityTemplate template, Collection<T> result, long total) {
		super(result, total);
		this.template = template;
	}

	public EntityTemplate getTemplate() {
		return template;
	}

	public void setTemplate(EntityTemplate template) {
		this.template = template;
	}

}
