package com.jadaptive.app.scheduler;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.cache.CacheService;
import com.sshtools.gardensched.ObjectStore;

public class SchedulerObjectStorage implements ObjectStore {

	@Autowired
	private CacheService cachService;

	@Override
	public boolean has(String path, Serializable key) {
		return cachService.getCacheOrCreate(path, Serializable.class, Serializable.class).containsKey(key);
	}

	@Override
	public Serializable get(String path, Serializable key) {
		return cachService.getCacheOrCreate(path, Serializable.class, Serializable.class).get(key);
	}

	@Override
	public void put(String path, Serializable key, Serializable value) {
		cachService.getCacheOrCreate(path, Serializable.class, Serializable.class).put(key, value);
	}

	@Override
	public boolean remove(String path, Serializable key) {
		return cachService.getCacheOrCreate(path, Serializable.class, Serializable.class).remove(key) != null;
	}
	
}
