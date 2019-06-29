package com.jadaptive.templates;

import org.springframework.stereotype.Repository;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.AbstractUUIDRepositoryImpl;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.utils.Version;

@Repository
public class TemplateRepositoryImpl extends AbstractUUIDRepositoryImpl<Template> implements TemplateRepository {

	@Override
	protected Class<Template> getResourceClass() {
		return Template.class;
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

		Template t;
		try {
			t = get(uuid);
			return new Version(t.getVersion());
		} catch (EntityNotFoundException e) {
			return null;
		}

	}

	@Override
	public Template createEntity() {
		return new Template();
	}

	@Override
	public String getName() {
		return "Template";
	}

}
