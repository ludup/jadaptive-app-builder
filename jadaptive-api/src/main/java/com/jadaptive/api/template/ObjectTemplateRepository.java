package com.jadaptive.api.template;

import java.util.Collection;

import com.jadaptive.api.tenant.AbstractTenantAwareObjectDatabase;

public interface ObjectTemplateRepository extends AbstractTenantAwareObjectDatabase<ObjectTemplate> {

	void createIndexes(ObjectTemplate template, Index[] nonUnique, UniqueIndex[] unique, boolean newSchema);

	Collection<ObjectTemplate> findReferences(ObjectTemplate template);

	boolean hasReferences(ObjectTemplate template);

}
