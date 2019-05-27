package com.jadaptive.templates;

import com.jadaptive.repository.AbstractUUIDRepository;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.utils.Version;

public interface TemplateRepository extends AbstractUUIDRepository<Template> {

	boolean hasProcessed(String uuid, String version);

	Version getCurrentVersion(String uuid) throws RepositoryException;

}
