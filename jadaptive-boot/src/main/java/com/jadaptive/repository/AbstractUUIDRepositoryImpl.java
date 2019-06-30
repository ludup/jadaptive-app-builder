package com.jadaptive.repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.db.Database;
import com.jadaptive.db.RedisDatabase;
import com.jadaptive.entity.EntityNotFoundException;


public abstract class AbstractUUIDRepositoryImpl<E extends AbstractUUIDEntity> extends AbstractLoggingServiceImpl implements AbstractUUIDRepository<E> {

	private static final String SYSTEM_UUID = "system";

	protected abstract Class<E> getResourceClass();
	
	static Database datasource = new RedisDatabase(); 
	static Map<Class<?>, AbstractUUIDRepository<?>> repositories = new HashMap<>();
	
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
	public void saveObject(AbstractUUIDEntity e) throws RepositoryException {
		save((E)e);
	}
			
	@Override
	@SafeVarargs
	public final void save(E e, TransactionAdapter<E>... operations) throws RepositoryException {
		try {
			
			Map<String,Map<String,String>> properties =  new HashMap<>();
			e.store(properties);
			
			datasource.save(getTenantUuid(), getResourceClass().getName(), e.getUuid(), properties);
			
			for(TransactionAdapter<E> op : operations) {
				op.afterSave(e);
			}
		} catch (ParseException ex) {
			throw new RepositoryException(ex.getMessage(), ex);
		}
	}
	
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
			obj.load(uuid, properties);
			return obj;
		} catch (ParseException e) {
			throw new RepositoryException(e);
		}
	}



	@Override
	public Collection<E> list() throws RepositoryException {
		try {
			List<E> results = new ArrayList<>();
			for(String uuid : datasource.list(getTenantUuid(), getResourceClass().getName()))  {
				E e = createEntity();
				e.load(uuid, datasource.get(getTenantUuid(), getResourceClass().getName(), uuid));
				results.add(e);
			}
			return results;
		} catch (ParseException | EntityNotFoundException e) {
			throw new RepositoryException(e);
		}
	}

}
