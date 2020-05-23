package com.jadaptive.api.template;

import com.jadaptive.api.tenant.AbstractTenantAwareObjectDatabase;

public interface ObjectTemplateRepository extends AbstractTenantAwareObjectDatabase<ObjectTemplate> {

	void createIndexes(ObjectTemplate template, Index[] nonUnique, UniqueIndex[] unique);

}
