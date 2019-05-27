package com.jadaptive.entity.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.templates.TemplateEnabledUUIDRepositoryImpl;
import com.jadaptive.tenant.TenantService;

@Repository
public class EntityTemplateRepositoryImpl extends TemplateEnabledUUIDRepositoryImpl<EntityTemplate> implements EntityTemplateRepository {

	@Autowired
	TenantService tenantService;
	
	@Override
	public Integer getWeight() {
		return 0;
	}

	@Override
	protected Class<EntityTemplate> getResourceClass() {
		return EntityTemplate.class;
	}

	@Override
	protected EntityTemplate createEntity() {
		return new EntityTemplate();
	}

	@Override
	protected String getName() {
		return "EntityTemplate";
	}

}
