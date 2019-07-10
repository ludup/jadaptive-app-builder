package com.jadaptive.entity;

import org.springframework.stereotype.Repository;

import com.jadaptive.repository.TenantAwareUUIDRepositoryImpl;
import com.jadaptive.templates.SystemTemplates;

@Repository
public class EntityRepositoryImpl extends TenantAwareUUIDRepositoryImpl<Entity> implements EntityRepository {

	@Override
	public Integer getWeight() {
		return SystemTemplates.ENTITY.ordinal();
	}

	@Override
	protected Class<Entity> getResourceClass() {
		return Entity.class;
	}

	@Override
	public String getName() {
		return "Entity";
	}

	@Override
	public Entity createEntity() {
		return new Entity();
	}

	@Override
	protected boolean isAutomaticResourceKey() {
		return false;
	}

}
