package com.jadaptive.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.db.Database;
import com.jadaptive.db.RedisDatabase;
import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.tenant.TenantService;


public abstract class AbstractUUIDRepositoryImpl<E extends AbstractUUIDEntity> extends AbstractLoggingServiceImpl implements AbstractUUIDRepository<E> {

	protected abstract Class<E> getResourceClass();
	
	static Database datasource = new RedisDatabase(); 
	static Map<Class<?>, AbstractUUIDRepository<?>> repositories = new HashMap<>();
	
	@Autowired
	TenantService tenantService; 
	
	@PostConstruct
	private void postConstruct() {
		repositories.put(getResourceClass(), this);
	}

	public static AbstractUUIDRepository<?> getRepositoryForType(Class<?> clz) {
		AbstractUUIDRepository<?> repository = repositories.get(clz);
		return repository;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void save(List<E> objects, TransactionAdapter<E>... operations) throws RepositoryException {
		for(E e : objects) {
			save(e, operations);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void save(List<E> objects, String resourceKey, TransactionAdapter<E>... operations) throws RepositoryException {
		for(E e : objects) {
			save(e, resourceKey, operations);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void saveObject(AbstractUUIDEntity e) throws RepositoryException {
		save((E)e);
	}
			
	@Override
	@SafeVarargs
	public final void save(E e, TransactionAdapter<E>... operations) throws RepositoryException {

			
		Map<String,Map<String,String>> properties =  new HashMap<>();
		
		datasource.save(tenantService.getCurrentTenant(), e, properties);
		
		for(TransactionAdapter<E> op : operations) {
			op.afterSave(e);
		}
		
	}
	
	@Override
	@SafeVarargs
	public final void save(E e, String resourceKey, TransactionAdapter<E>... operations) throws RepositoryException {

		Map<String,Map<String,String>> properties =  new HashMap<>();
		
		datasource.save(tenantService.getCurrentTenant(), e, resourceKey, properties);
		
		for(TransactionAdapter<E> op : operations) {
			op.afterSave(e);
		}
		
	}

	
	@Override
	public E get(String uuid) throws RepositoryException, EntityNotFoundException {
		return datasource.get(tenantService.getCurrentTenant(), uuid, getResourceClass());
	}
	
	
	
	@Override
	public E get(String uuid, String resourceKey) throws RepositoryException, EntityNotFoundException {
		return datasource.get(tenantService.getCurrentTenant(), uuid, resourceKey, getResourceClass());
	}

	@Override
	public Collection<E> list(String resourceKey) throws RepositoryException {
		return datasource.list(tenantService.getCurrentTenant(), resourceKey, getResourceClass());
	}

	@Override
	public Collection<E> list() throws RepositoryException {
		return datasource.list(tenantService.getCurrentTenant(), getResourceClass());
	}
	
	@Override
	public void delete(String resourceKey, String uuid) {
		datasource.delete(tenantService.getCurrentTenant(), resourceKey, uuid);
	}
	
	@Override
	public void delete(String uuid) {
		datasource.delete(tenantService.getCurrentTenant(), getResourceClass(), uuid);
	}

}
