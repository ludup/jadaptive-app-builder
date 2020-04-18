package com.jadaptive.api.tasks;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Template;

@Template(name = "Task", resourceKey = "task", type = EntityType.OBJECT)
public abstract class Task extends AbstractUUIDEntity {

	
}
