package com.jadaptive.api.template;

import com.jadaptive.api.tenant.AbstractTenantAwareObjectDatabase;

public interface EntityTemplateRepository extends AbstractTenantAwareObjectDatabase<EntityTemplate> {

	void createIndexes(EntityTemplate template, Index[] nonUnique, UniqueIndex[] unique);

}
