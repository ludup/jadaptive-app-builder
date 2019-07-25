package com.jadaptive.templates;

import java.util.Collection;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;

public interface TemplateVersionService {

	Collection<TemplateVersion> list() throws RepositoryException, EntityException;

	<E extends AbstractUUIDEntity>  void processTemplates(TemplateEnabledService<E> repository);

}
