package com.jadaptive.app.templates;

import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.templates.TemplateVersion;
import com.jadaptive.api.templates.TemplateVersionRepository;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.tenant.AbstractSystemObjectDatabaseImpl;
import com.jadaptive.utils.Version;

@Repository
public class TemplateVersionRepositoryImpl extends AbstractSystemObjectDatabaseImpl<TemplateVersion> implements TemplateVersionRepository {

	@Autowired
	private TenantService tenantService; 

	@Autowired
	private CacheService cacheService; 
	
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
		} catch (ObjectException e) {
			return null;
		}

	}

	@Override
	public void save(TemplateVersion version) throws RepositoryException, ObjectException {
		saveObject(version, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public Iterable<TemplateVersion> list() throws RepositoryException, ObjectException {
		return listObjects(tenantService.getCurrentTenant().getUuid(), TemplateVersion.class);
	}

	@Override
	protected <T extends UUIDEntity> Cache<String, T> getCache(Class<T> obj) {
		return cacheService.getCacheOrCreate("templateVersions.uuidCache", String.class, obj);
	}

	@Override
	public Class<TemplateVersion> getResourceClass() {
		return TemplateVersion.class;
	}	
}
