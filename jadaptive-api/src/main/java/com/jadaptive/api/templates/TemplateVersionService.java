package com.jadaptive.templates;

import java.util.Collection;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.Tenant;

public interface TemplateVersionService {

	Collection<TemplateVersion> list() throws RepositoryException, EntityException;

	<E extends AbstractUUIDEntity>  void processTemplates(Tenant tenant, TemplateEnabledService<E> repository);

}
