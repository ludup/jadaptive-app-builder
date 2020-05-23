package com.jadaptive.api.templates;

import java.util.Collection;

import com.jadaptive.api.db.AbstractObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.utils.Version;

public interface TemplateVersionRepository extends AbstractObjectDatabase {

	boolean hasProcessed(String uuid, String version);

	Version getCurrentVersion(String uuid) throws RepositoryException;

	void save(TemplateVersion version) throws RepositoryException, ObjectException;

	Collection<TemplateVersion> list() throws RepositoryException, ObjectException;

}
