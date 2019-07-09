package com.jadaptive.db;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantServiceImpl;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisDatabase implements Database {

	RedisClient redisClient;
	StatefulRedisConnection<String, String> connection;
	
	public RedisDatabase() {
		
		 this.redisClient = RedisClient
				  .create("redis://127.0.0.1:32783/");
				
		this.connection = redisClient.connect();
	}

	@Override
	public String getName() {
		return "redis";
	}

	@Override
	public void save(Tenant tenant, AbstractUUIDEntity obj, Map<String, Map<String, String>> additionalProperties) {
		doSave(tenant.getUuid(), obj, additionalProperties);
	}
	
	@Override
	public void save(AbstractUUIDEntity obj, Map<String, Map<String, String>> additionalProperties) {
		doSave(TenantServiceImpl.SYSTEM_TENANT_UUID, obj, additionalProperties);
	}
	
	protected void doSave(String tenantUUID, AbstractUUIDEntity obj, Map<String, Map<String, String>> additionalProperties) {
		try {
			RedisCommands<String, String> syncCommands = connection.sync();
			String className = obj.getClass().getName();
			
			syncCommands.sadd(getClassIndex(tenantUUID, className), obj.getUuid());
			
			
			Map<String,Map<String,String>> properties = new HashMap<>();
			obj.store(properties);
			
			if(!Objects.isNull(additionalProperties)) {
				properties.putAll(additionalProperties);
			}
			
			String key = getObjectKey(tenantUUID, className, obj.getUuid());
			
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
	public <T extends AbstractUUIDEntity> T get(Tenant tenant, String uuid, Class<T> clz)
			throws EntityNotFoundException {
		return doGet(tenant.getUuid(), uuid, clz);
	}
	
	@Override
	public <T extends AbstractUUIDEntity> T get(String uuid, Class<T> clz)
			throws EntityNotFoundException {
		return doGet(TenantServiceImpl.SYSTEM_TENANT_UUID, uuid, clz);
	}
	
	protected <T extends AbstractUUIDEntity> T doGet(String tenant, String uuid, Class<T> clz)
			throws EntityNotFoundException {
		
		try {
			RedisCommands<String, String> syncCommands = connection.sync();
			String className = clz.getName();
			
			Map<String,Map<String,String>> properties = new HashMap<>();
			
			for(String key : syncCommands.smembers(getObjectKey(tenant, className, uuid))) {
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
		return doList(tenant.getUuid(), clz);
	}
	
	@Override
	public <T extends AbstractUUIDEntity> Collection<T> list(Class<T> clz) {
		return doList(TenantServiceImpl.SYSTEM_TENANT_UUID, clz);
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> doList(String tenant, Class<T> clz) {
		try {
			RedisCommands<String, String> syncCommands = connection.sync();
			String className = clz.getName();
			
			List<T> results = new ArrayList<>();
			for(String uuid : syncCommands.smembers(getClassIndex(tenant, className))) {
				results.add(doGet(tenant, uuid, clz));
			}
			
			return results;
		} catch (EntityNotFoundException e) {
			throw new RepositoryException(e);
		}
	}

}
