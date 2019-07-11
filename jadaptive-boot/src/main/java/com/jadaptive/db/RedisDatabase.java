package com.jadaptive.db;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantServiceImpl;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisDatabase extends AbstractLoggingServiceImpl implements Database {

	RedisClient redisClient;
	StatefulRedisConnection<String, String> connection;
	
	public RedisDatabase() {
		
		 this.redisClient = RedisClient
				  .create("redis://127.0.0.1:32800/");
				
		this.connection = redisClient.connect();
	}

	@Override
	public String getName() {
		return "redis";
	}
	
	@Override
	public void save(Tenant tenant, AbstractUUIDEntity obj, String resourceKey, Map<String, Map<String, String>> additionalProperties) {
		doSave(tenant.getUuid(), obj, resourceKey, additionalProperties);
	}
	
	@Override
	public void save(AbstractUUIDEntity obj, String resourceKey, Map<String, Map<String, String>> additionalProperties) {
		doSave(TenantServiceImpl.SYSTEM_TENANT_UUID, obj, resourceKey, additionalProperties);
	}

	@Override
	public void save(Tenant tenant, AbstractUUIDEntity obj, Map<String, Map<String, String>> additionalProperties) {
		doSave(tenant.getUuid(), obj, obj.getClass().getName(), additionalProperties);
	}
	
	@Override
	public void save(AbstractUUIDEntity obj, Map<String, Map<String, String>> additionalProperties) {
		doSave(TenantServiceImpl.SYSTEM_TENANT_UUID, obj, obj.getClass().getName(), additionalProperties);
	}
	
	protected void doSave(String tenantUUID, AbstractUUIDEntity obj, String resourceKey, Map<String, Map<String, String>> additionalProperties) {
		try {
			RedisCommands<String, String> syncCommands = connection.sync();
			
			
			syncCommands.sadd(getClassIndex(tenantUUID, resourceKey), obj.getUuid());
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Saving resource %s with uuid %s", resourceKey, obj.getUuid()));
			}
			
			Map<String,Map<String,String>> properties = new HashMap<>();
			obj.store(properties);
			
			if(!Objects.isNull(additionalProperties)) {
				properties.putAll(additionalProperties);
			}
			
			String key = getObjectKey(tenantUUID, resourceKey, obj.getUuid());
			
			for(Map.Entry<String, Map<String,String>> entry : properties.entrySet()) {
				syncCommands.sadd(key, entry.getKey());
				syncCommands.hmset(entry.getKey(), entry.getValue());
			}
		} catch (ParseException e) {
			throw new RepositoryException(e);
		}
	}

	private String getClassIndex(String tenant, String className) {
		return String.format("%s:%s:index", tenant, className);
	}
	
	private String getObjectKey(String tenant, String className, String uuid) {
		return String.format("%s:%s:%s", tenant, className, uuid);
	}

	
	@Override
	public <T extends AbstractUUIDEntity> T get(Tenant tenant, String uuid, String resourceKey, Class<T> clz)
			throws EntityNotFoundException {
		return doGet(tenant.getUuid(), uuid, resourceKey, clz);
	}
	
	@Override
	public <T extends AbstractUUIDEntity> T get(Tenant tenant, String uuid, Class<T> clz)
			throws EntityNotFoundException {
		return doGet(tenant.getUuid(), uuid, clz.getName(), clz);
	}
	
	@Override
	public <T extends AbstractUUIDEntity> T get(String uuid, String resourceKey, Class<T> clz)
			throws EntityNotFoundException {
		return doGet(TenantServiceImpl.SYSTEM_TENANT_UUID, uuid, resourceKey, clz);
	}
	
	@Override
	public <T extends AbstractUUIDEntity> T get(String uuid, Class<T> clz)
			throws EntityNotFoundException {
		return doGet(TenantServiceImpl.SYSTEM_TENANT_UUID, uuid, clz.getName(), clz);
	}
	
	protected <T extends AbstractUUIDEntity> T doGet(String tenant, String uuid, String resourceKey, Class<T> clz)
			throws EntityNotFoundException {
		
		try {
			RedisCommands<String, String> syncCommands = connection.sync();
			
			Map<String,Map<String,String>> properties = new HashMap<>();
			
			for(String key : syncCommands.smembers(getObjectKey(tenant, resourceKey, uuid))) {
				properties.put(key, syncCommands.hgetall(key));
			}
			
			if(properties.isEmpty()) {
				throw new EntityNotFoundException(String.format("Cannot find entity with uuid %s", uuid));
			}
			
			T obj = clz.newInstance();
			obj.load(uuid, properties);
			return obj;
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public <T extends AbstractUUIDEntity> Collection<T> list(Tenant tenant, Class<T> clz) {
		return doList(tenant.getUuid(), clz.getName(), clz);
	}
	
	
	@Override
	public <T extends AbstractUUIDEntity> Collection<T> list(Tenant tenant, String className, Class<T> clz) {
		return doList(tenant.getUuid(), className, clz);
	}
	
	@Override
	public <T extends AbstractUUIDEntity> Collection<T> list(Class<T> clz) {
		return doList(TenantServiceImpl.SYSTEM_TENANT_UUID, clz.getName(), clz);
	}
	
	@Override
	public <T extends AbstractUUIDEntity> Collection<T> list(String resourceKey, Class<T> clz) {
		return doList(TenantServiceImpl.SYSTEM_TENANT_UUID, resourceKey, clz);
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> doList(String tenant, String resourceKey, Class<T> clz) {
		try {
			RedisCommands<String, String> syncCommands = connection.sync();
			
			List<T> results = new ArrayList<>();
			for(String uuid : syncCommands.smembers(getClassIndex(tenant, resourceKey))) {
				results.add(doGet(tenant, uuid, resourceKey, clz));
			}
			
			return results;
		} catch (EntityNotFoundException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void delete(Tenant tenant, Class<?> resourceClass, String uuid) {
		doDelete(tenant.getUuid(), resourceClass.getName(), uuid);
	}

	@Override
	public void delete(Tenant tenant, String resourceKey, String uuid) {
		doDelete(tenant.getUuid(), resourceKey, uuid);
	}
	
	protected void doDelete(String tenant, String resourceKey, String uuid) {
			
		RedisCommands<String, String> syncCommands = connection.sync();
		
		String key = getObjectKey(tenant, resourceKey, uuid);
		syncCommands.del(syncCommands.smembers(key).toArray(new String[0]));
		
		String index = getClassIndex(tenant, resourceKey);
		syncCommands.srem(index, uuid);
		
	}

	@Override
	public void deleteAll(Tenant tenant, String resourceKey) {
		doDeleteAll(tenant.getUuid(), resourceKey);
	}

	protected void doDeleteAll(String tenant, String resourceKey) {
		RedisCommands<String, String> syncCommands = connection.sync();
		
		String index = getClassIndex(tenant, resourceKey);
		String[] members = syncCommands.smembers(index).toArray(new String[0]);
		
		if(members.length > 0) {
			for(String uuid : members) {
				String key = getObjectKey(tenant, resourceKey, uuid);
				syncCommands.del(syncCommands.smembers(key).toArray(new String[0]));
			}
	
			syncCommands.srem(index, members);
		}
	}
}
