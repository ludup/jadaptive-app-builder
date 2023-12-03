package com.jadaptive.api.tasks;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = Task.RESOURCE_KEY, type = ObjectType.OBJECT)
public abstract class Task extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "task";
	private static final long serialVersionUID = -4801145916130392635L;

	
}
