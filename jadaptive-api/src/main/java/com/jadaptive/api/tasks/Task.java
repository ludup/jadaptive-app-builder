package com.jadaptive.api.tasks;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(name = "Task", resourceKey = "task", type = ObjectType.OBJECT)
public abstract class Task extends AbstractUUIDEntity {

	private static final long serialVersionUID = -4801145916130392635L;

	
}
