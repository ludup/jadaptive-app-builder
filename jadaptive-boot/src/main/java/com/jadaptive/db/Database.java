package com.jadaptive.db;

import java.util.Collection;
import java.util.Map;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.tenant.Tenant;

public interface Database {

	String getName();
	
	void save(Tenant tenant, AbstractUUIDEntity obj, Map<String,Map<String,String>> values);
	
	<T extends AbstractUUIDEntity> T get(Tenant tenant, String uuid, Class<T> clz) throws EntityNotFoundException;

	<T extends AbstractUUIDEntity> Collection<T> list(Tenant tenant, Class<T> clz);

	void save(AbstractUUIDEntity obj, Map<String, Map<String, String>> additionalProperties);

	<T extends AbstractUUIDEntity> T get(String uuid, Class<T> clz) throws EntityNotFoundException;

	<T extends AbstractUUIDEntity> Collection<T> list(Class<T> clz);
}
