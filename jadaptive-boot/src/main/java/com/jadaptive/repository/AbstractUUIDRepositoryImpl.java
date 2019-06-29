package com.jadaptive.repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.datasource.DataSourceEntity;
import com.jadaptive.datasource.EntityDataSource;
import com.jadaptive.datasource.RedisDataSource;
import com.jadaptive.entity.EntityNotFoundException;


public abstract class AbstractUUIDRepositoryImpl<E extends AbstractUUIDEntity> extends AbstractLoggingServiceImpl implements AbstractUUIDRepository<E> {

	private static final String SYSTEM_UUID = "system";

	protected abstract Class<E> getResourceClass();
	
	EntityDataSource datasource = new RedisDataSource(); 
	
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
			datasource.save(getTenantUuid(), getResourceClass().getName(), e.getUuid(), properties);
			
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
//		for(Map<String,String> entityService : datasource.list(resourceKey))  {
//			results.add(buildEntity(entityService));
//		}
//		return results;
//	}
	
	protected String getTenantUuid() {
		return SYSTEM_UUID;
	}
	
	@Override
	public E get(String uuid) throws RepositoryException, EntityNotFoundException {
		Map<String,Map<String,String>> e = datasource.get(getTenantUuid(), getResourceClass().getName(), uuid);
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
	public Collection<E> list() throws RepositoryException {
		try {
			List<E> results = new ArrayList<>();
			for(String uuid : datasource.list(getTenantUuid(), getResourceClass().getName()))  {
				E e = createEntity();
				((DataSourceEntity)e).load(uuid, datasource.get(getTenantUuid(), getResourceClass().getName(), uuid));
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
