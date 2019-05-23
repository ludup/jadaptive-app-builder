package com.jadaptive.templates;

import com.jadaptive.Version;
import com.jadaptive.repository.AbstractUUIDRepository;
import com.jadaptive.repository.RepositoryException;

public interface TemplateRepository extends AbstractUUIDRepository<Template> {

	boolean hasProcessed(String resourceKey, String version);

	Version getCurrentVersion(String resourceKey) throws RepositoryException;

}
