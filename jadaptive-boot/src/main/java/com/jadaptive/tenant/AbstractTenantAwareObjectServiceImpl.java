package com.jadaptive.tenant;

import java.util.Collection;
import java.util.Objects;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;

public abstract class AbstractTenantAwareObjectServiceImpl<T extends AbstractUUIDEntity> implements AbstractTenantAwareObjectService<T> {

	
	protected abstract AbstractTenantAwareObjectDatabase<T> getRepository();
	
	@Override
	public T get(String uuid) throws RepositoryException, EntityException {
		
		
		T e = getRepository().get(uuid);
		
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Cannot find object with uuid %s", uuid));
		}
		
		return e;
	}

	@Override
	public Collection<T> list() throws RepositoryException, EntityException {
		
		return getRepository().list();
	}
	
	@Override
	public Collection<T> table(String search, String order, int start, int length) throws RepositoryException, EntityException {
		
		return getRepository().table(search, order, start, length);
	}
	
	@Override
	public long count() {
		return getRepository().count();
	}

	@Override
	public void saveOrUpdate(T obj) throws RepositoryException, EntityException {
		
		getRepository().saveOrUpdate(obj);
		
	}

	@Override
	public void delete(T obj) throws EntityException {
		delete(obj.getUuid());
	}
	
	@Override
	public void delete(String uuid) throws EntityException {
		
		beforeDelete(uuid);
		getRepository().delete(uuid);
		afterDelete(uuid);
	}

	protected void afterDelete(String uuid) {

	}

	protected void beforeDelete(String uuid) {
		
	}
}
