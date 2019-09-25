package com.jadaptive.json;

import com.jadaptive.entity.template.EntityTemplate;

public class EntityTableStatus<T> extends EntityStatus<T> {

	EntityTemplate template;
	
	public EntityTableStatus(boolean success, String message) {
		super(success, message);
	}
	
	public EntityTableStatus(EntityTemplate template, T result) {
		super(result);
		this.template = template;
	}

	public EntityTemplate getTemplate() {
		return template;
	}

	public void setTemplate(EntityTemplate template) {
		this.template = template;
	}

}
