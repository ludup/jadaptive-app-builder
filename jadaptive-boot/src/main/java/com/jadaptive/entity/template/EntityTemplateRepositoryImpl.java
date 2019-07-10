package com.jadaptive.entity.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.repository.TenantAwareUUIDRepositoryImpl;
import com.jadaptive.templates.SystemTemplates;
import com.jadaptive.tenant.TenantService;

@Repository
public class EntityTemplateRepositoryImpl extends TenantAwareUUIDRepositoryImpl<EntityTemplate> implements EntityTemplateRepository {

	@Autowired
	TenantService tenantService;
	
	@Override
	public Integer getWeight() {
		return SystemTemplates.ENTITY_TEMPLATE.ordinal();
	}

	@Override
	protected Class<EntityTemplate> getResourceClass() {
		return EntityTemplate.class;
	}

	@Override
	public EntityTemplate createEntity() {
		return new EntityTemplate();
	}

	@Override
	public String getName() {
		return "EntityTemplate";
	}

	@Override
	protected boolean isAutomaticResourceKey() {
		return true;
	}

}
