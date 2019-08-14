package com.jadaptive.json;

import com.jadaptive.entity.template.EntityTemplate;

public class TableStatus<T> extends EntityStatus<T> {

	EntityTemplate template;
	
	public TableStatus(boolean success, String message) {
		super(success, message);
	}
	
	public TableStatus(EntityTemplate template, T result) {
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
