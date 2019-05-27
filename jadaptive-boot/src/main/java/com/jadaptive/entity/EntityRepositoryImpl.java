package com.jadaptive.entity;

import org.springframework.stereotype.Repository;

import com.jadaptive.templates.TemplateEnabledUUIDRepositoryImpl;

@Repository
public class EntityRepositoryImpl extends TemplateEnabledUUIDRepositoryImpl<Entity> implements EntityRepository {

	@Override
	public Integer getWeight() {
		return 1;
	}

	@Override
	protected Class<Entity> getResourceClass() {
		return Entity.class;
	}

	@Override
	protected String getName() {
		return "Entity";
	}

	@Override
	protected Entity createEntity() {
		return new Entity();
	}

}
