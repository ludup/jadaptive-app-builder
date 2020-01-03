package com.jadaptive.entity.template;

import org.springframework.stereotype.Repository;

import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.tenant.AbstractTenantAwareObjectDatabaseImpl;

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
