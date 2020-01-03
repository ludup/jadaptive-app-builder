package com.jadaptive.app.templates;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.templates.TemplateVersion;
import com.jadaptive.api.templates.TemplateVersionRepository;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.AbstractObjectDatabaseImpl;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.repository.RepositoryException;
import com.jadaptive.utils.Version;

@Repository
public class TemplateVersionRepositoryImpl extends AbstractObjectDatabaseImpl implements TemplateVersionRepository {

	@Autowired
	TenantService tenantService; 

	TemplateVersionRepositoryImpl(DocumentDatabase db) {
		super(db);
	}
	
	@Override
	public boolean hasProcessed(String uuid, String version) {
		try {
			
			Version v = new Version(version);
			Version v2 = getCurrentVersion(uuid);
			if(v2==null) {
				return false;
			}
			return v2.compareTo(v) > 0;
		} catch (RepositoryException e) {
			throw new  IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public Version getCurrentVersion(String uuid) throws RepositoryException {

		TemplateVersion t;
		try {
			t = getObject(uuid, tenantService.getCurrentTenant().getUuid(), TemplateVersion.class);
			return new Version(t.getVersion());
		} catch (EntityException e) {
			return null;
		}

	}

	@Override
	public void save(TemplateVersion version) throws RepositoryException, EntityException {
		saveObject(version, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public Collection<TemplateVersion> list() throws RepositoryException, EntityException {
		return listObjects(tenantService.getCurrentTenant().getUuid(), TemplateVersion.class);
	}
}