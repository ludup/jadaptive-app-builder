package com.jadaptive.app.scheduler;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.tenant.TenantService;
import com.sshtools.gardensched.ObjectStore;

public class SchedulerObjectStorage implements ObjectStore {
	
	@Autowired
	private CacheService cachService;
	@Autowired
	private TenantService tenantService;

	@Override
	public boolean has(String path, Serializable key) {
		return doWithTenant(path, (ppath) ->
			cachService.getCacheOrCreate(ppath, Serializable.class, Serializable.class).containsKey(key)
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> Set<T> keySet(String path) {
		return (Set<T>) doWithTenant(path, (ppath) ->
			cachService.getCacheOrCreate(ppath, Serializable.class, Serializable.class).keySet()
		);
	}

	@Override
	public int size(String path) {
		return doWithTenant(path, (ppath) ->
			cachService.getCacheOrCreate(ppath, Serializable.class, Serializable.class).size()
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String path, Serializable key) {
		return (T)doWithTenant(path, (ppath) ->
			cachService.getCacheOrCreate(ppath, Serializable.class, Serializable.class).get(key)
		);
	}

	@Override
	public void put(String path, Serializable key, Serializable value) {

		if (value == null) {
			remove(path, key);
		} else {
			doWithTenant(path, (ppath) -> {
				var cache = cachService.getCacheOrCreate(ppath, Serializable.class, Serializable.class);
				cache.put(key, value);
				return null;
			});
		}
	}

	@Override
	public boolean remove(String path, Serializable key) {
		return doWithTenant(path, (ppath) ->
			cachService.getCacheOrCreate(ppath, Serializable.class, Serializable.class).remove(key) != null
		);
	}
	
	private <T> T doWithTenant(String path, Function<String, T> callback) {
		var idx = path.indexOf("/");
		if(idx > -1) {
			var tenantId = path.substring(0, idx);
			path = path.substring(idx + 1);
			try(var tctx = tenantService.tenant(tenantService.getTenantByUUID(tenantId))) {
				return callback.apply(path);
			}
		}
		else {
			return callback.apply(path);
		}
	}
	
}
