package com.jadaptive.templates;

import java.util.Collection;

import com.jadaptive.db.AbstractObjectDatabase;
import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.utils.Version;

public interface TemplateVersionRepository extends AbstractObjectDatabase {

	boolean hasProcessed(String uuid, String version);

	Version getCurrentVersion(String uuid) throws RepositoryException;

	void save(TemplateVersion version) throws RepositoryException, EntityException;

	Collection<TemplateVersion> list() throws RepositoryException, EntityException;

}
