package com.jadaptive.db;

import java.util.Collection;
import java.util.Map;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.tenant.Tenant;

public interface Database {

	String getName();
	
	
	<T extends AbstractUUIDEntity> T get(Tenant tenant, String uuid, Class<T> clz) throws EntityException;

	<T extends AbstractUUIDEntity> T get(String uuid, String resourceKey, Class<T> clz) throws EntityException;

	<T extends AbstractUUIDEntity> T get(Tenant tenant, String uuid, String resourceKey, Class<T> clz) throws EntityException;	

	<T extends AbstractUUIDEntity> T get(String uuid, Class<T> clz) throws EntityException;

	<T extends AbstractUUIDEntity> Collection<T> list(Tenant tenant, Class<T> clz);

	<T extends AbstractUUIDEntity> Collection<T> list(Class<T> clz);

	<T extends AbstractUUIDEntity> Collection<T> list(Tenant tenant, String resourceKey, Class<T> clz);

	<T extends AbstractUUIDEntity> Collection<T> list(String resourceKey, Class<T> clz);

	void save(Tenant tenant, AbstractUUIDEntity obj, Map<String,Map<String,String>> values);
	
	void save(AbstractUUIDEntity obj, Map<String, Map<String, String>> additionalProperties);
	
	void save(Tenant tenant, AbstractUUIDEntity obj, String resourceKey,
			Map<String, Map<String, String>> additionalProperties);

	void save(AbstractUUIDEntity obj, String resourceKey, Map<String, Map<String, String>> additionalProperties);

	void delete(Tenant tenant, Class<?> resourceClass, String uuid);
	
	void delete(Tenant tenant, String resourceKey, String uuid);

	void deleteAll(Tenant currentTenant, String resourceKey);
}
