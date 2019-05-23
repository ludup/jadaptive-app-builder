package com.jadaptive.repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.AbstractLoggingServiceImpl;
import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.entity.repository.AbstractUUIDEntityImpl;
import com.jadaptive.entity.repository.DataSourceEntity;
import com.jadaptive.entity.repository.EntityDataSource;
import com.jadaptive.entity.repository.RedisDataSource;
import com.jadaptive.templates.TransactionAdapter;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantService;


public abstract class AbstractUUIDRepositoryImpl<E extends AbstractUUIDEntityImpl> extends AbstractLoggingServiceImpl implements AbstractUUIDRepository<E> {

	protected abstract Class<E> getResourceClass();
	
	EntityDataSource datasource = new RedisDataSource();
	
	@Autowired
	TenantService tenantService; 
	
	@SuppressWarnings("unchecked")
	@Override
	public void save(List<E> objects, TransactionAdapter<E>... operations) throws RepositoryException {
		for(E e : objects) {
			save(e, operations);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void save(E e, TransactionAdapter<E>... operations) throws RepositoryException {
		try {
			
			if(!(e instanceof DataSourceEntity)) {
				throw new RepositoryException("A root entity must implement DataSourceEntity");
			}
			Map<String,Map<String,String>> properties =  new HashMap<>();
			((DataSourceEntity)e).store(properties);
			datasource.save(tenantService.getCurrentTenant(), getResourceClass().getName(), e.getUuid(), properties);
			
			for(TransactionAdapter<E> op : operations) {
				op.afterSave(e);
			}
		} catch (ParseException ex) {
			throw new RepositoryException(ex.getMessage(), ex);
		}
	}
	
//	@Override
//	public List<E> list(String resourceKey) throws RepositoryException {
//		List<E> results = new ArrayList<>();
//		for(Map<String,String> e : datasource.list(resourceKey))  {
//			results.add(buildEntity(e));
//		}
//		return results;
//	}
	
	@Override
	public E get(String uuid) throws RepositoryException, EntityNotFoundException {
		Map<String,Map<String,String>> e = datasource.get(tenantService.getCurrentTenant(), getResourceClass().getName(), uuid);
		if(Objects.isNull(e)) {
			return null;
		}
		return buildEntity(uuid, e);
	}
	
	private E buildEntity(String uuid, Map<String,Map<String,String>> properties) throws RepositoryException {

		try {
			E obj = createEntity();
			if(!(obj instanceof DataSourceEntity)) {
				throw new RepositoryException("A root entity must implement DataSourceEntity");
			}
			((DataSourceEntity)obj).load(uuid, properties);
			return obj;
		} catch (ParseException e) {
			throw new RepositoryException(e);
		}
		
	}

	protected abstract String getName();
	
	protected abstract E createEntity();

	@Override
	public Collection<E> list(Tenant tenant) throws RepositoryException {
		try {
			List<E> results = new ArrayList<>();
			for(String uuid : datasource.list(tenant, getResourceClass().getName()))  {
				E e = createEntity();
				((DataSourceEntity)e).load(uuid, datasource.get(tenant, getResourceClass().getName(), uuid));
				results.add(e);
			}
			return results;
		} catch (ParseException | EntityNotFoundException e) {
			throw new RepositoryException(e);
		}
	}
	
//	@Override
//	public E get(String resourceKey, String column, String value) throws RepositoryException {
//		List<E> list = list(resourceKey, new QueryParameters.Builder().and(column, value).build());
//		if(list.isEmpty()) {
//			return null;
//		}
//		if(list.size() > 0) {
//			log.warn(String.format("Too many results returned for entity %s with colummn %s and value %s",
//					getName(),
//					column,
//					value));
//		}
//		return list.iterator().next();
//	}

}
