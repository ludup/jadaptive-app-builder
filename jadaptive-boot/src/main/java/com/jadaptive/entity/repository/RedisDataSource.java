package com.jadaptive.entity.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.tenant.Tenant;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisDataSource implements EntityDataSource {

	RedisClient redisClient;
	StatefulRedisConnection<String, String> connection;
	
	public RedisDataSource() {
		
		 this.redisClient = RedisClient
				  .create("redis://192.168.99.100:32782/");
				
		this.connection = redisClient.connect();
	}

	@Override
	public String getName() {
		return "redis";
	}

	@Override
	public void save(Tenant tenant, String resourceKey, String rootUuid, Map<String, Map<String, String>> values) {
		
		RedisCommands<String, String> syncCommands = connection.sync();
		indexObjects(syncCommands, indexRoot(syncCommands, tenant, resourceKey, rootUuid), values);
	}

	private void indexObjects(RedisCommands<String, String> syncCommands, String key,
			Map<String, Map<String, String>> values) {
		
		for(Map.Entry<String, Map<String,String>> entry : values.entrySet()) {
			syncCommands.sadd(key, entry.getKey());
			syncCommands.hmset(entry.getKey(), entry.getValue());
		}
		
	}

	private String indexRoot(RedisCommands<String, String> syncCommands, Tenant tenant, String resourceKey, String rootUuid) {
		
		/**
		 * Add this root object to the tenant's index of objects for type "resourceKey"
		 */
		String indexKey = getIndexKey(tenant, resourceKey);
		syncCommands.sadd(indexKey, rootUuid);
		
		/**
		 * Return the unique key for this root object.
		 */
		return getObjectKey(tenant, resourceKey, rootUuid);
	}

	private String getIndexKey(Tenant tenant, String resourceKey) {
		return String.format("%s:%s", tenant.getUuid(), resourceKey);
	}

	private String getObjectKey(Tenant tenant, String resourceKey, String rootUuid) {
		return String.format("%s:%s:%s", tenant.getUuid(), resourceKey, rootUuid);
	}
	
	@Override
	public Map<String, Map<String, String>> get(Tenant tenant, String resourceKey, String rootUuid)  throws EntityNotFoundException{
		
		RedisCommands<String, String> syncCommands = connection.sync();
		Map<String,Map<String,String>> result = new HashMap<>();
		
		for(String uuid : syncCommands.smembers(getObjectKey(tenant, resourceKey, rootUuid))) {
			result.put(uuid, syncCommands.hgetall(uuid));
		}
		
		if(result.isEmpty()) {
			throw new EntityNotFoundException(String.format("Cannot find entity with uuid %s", rootUuid));
		}
		return result;
	}

	@Override
	public Collection<String> list(Tenant tenant, String resourceKey) {
		
		RedisCommands<String, String> syncCommands = connection.sync();
		
		return Collections.unmodifiableCollection(syncCommands.smembers(getIndexKey(tenant, resourceKey)));
	}

//	@Override
//	public void save(String resourceKey, String uuid, Map<String, String> values) {
//		
//		RedisCommands<String, String> syncCommands = connection.sync();
//		
//		syncCommands.sadd(resourceKey, uuid);
//		syncCommands.hmset(uuid, values);
//		syncCommands.m
//	}
//
//	@Override
//	public Map<String, String> get(String resourceKey, String uuid) {
//		
//		return null;
//	}

}
