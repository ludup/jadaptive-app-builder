package com.jadaptive.entity.template;

import org.springframework.stereotype.Repository;

import com.jadaptive.templates.TemplateEnabledUUIDRepositoryImpl;

@Repository
public class EntityTemplateRepositoryImpl extends TemplateEnabledUUIDRepositoryImpl<EntityTemplateImpl> implements EntityTemplateRepository {


	@Override
	public Integer getWeight() {
		return 0;
	}

	@Override
	protected Class<EntityTemplateImpl> getResourceClass() {
		return EntityTemplateImpl.class;
	}

	@Override
	protected EntityTemplateImpl createEntity() {
		return new EntityTemplateImpl();
	}

	@Override
	protected String getName() {
		return "EntityTemplate";
	}

	@Override
	public EntityTemplateImpl getByResourceKey(String resourceKey) {
		// TODO Auto-generated method stub
		return null;
	}

}
