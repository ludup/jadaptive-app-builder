package com.jadaptive.app.entity.template;

import org.springframework.stereotype.Repository;

import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateRepository;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.tenant.AbstractTenantAwareObjectDatabaseImpl;

@Repository
public class EntityTemplateRepositoryImpl extends AbstractTenantAwareObjectDatabaseImpl<EntityTemplate>
		implements EntityTemplateRepository {

	public EntityTemplateRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	public Class<EntityTemplate> getResourceClass() {
		return EntityTemplate.class;
	}



}
