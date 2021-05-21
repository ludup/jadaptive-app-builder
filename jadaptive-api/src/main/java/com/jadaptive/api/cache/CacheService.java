package com.jadaptive.api.cache;

import java.util.Map;


public interface CacheService {

	public <K,V> Map<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value);
	
	public <K,V> Map<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value, long expiryTime);
	
	public <K,V> Map<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value);
}
