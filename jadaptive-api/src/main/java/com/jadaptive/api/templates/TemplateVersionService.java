package com.jadaptive.api.templates;

import java.util.Collection;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.tenant.Tenant;

public interface TemplateVersionService {

	Collection<TemplateVersion> list() throws RepositoryException, ObjectException;

	<E extends AbstractUUIDEntity>  void processTemplates(Tenant tenant, JsonTemplateEnabledService<E> repository);

	void registerAnnotatedTemplates();

}
