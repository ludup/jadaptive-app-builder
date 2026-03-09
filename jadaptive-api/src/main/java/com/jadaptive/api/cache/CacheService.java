package com.jadaptive.api.cache;

import java.util.Map;


public interface CacheService {

	<K,V> Map<K, V> clusteredCacheOrCreate(String name,Class<K> key, Class<V> value);
	
	<K,V> Map<K, V> clusteredCacheOrCreate(String name,Class<K> key, Class<V> value, long expiryTime);
	
	<K,V> Map<K, V> clusteredCacheIfExists(String name, Class<K> key, Class<V> value);

	<K,V> Map<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value);
	
	<K,V> Map<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value, long expiryTime);
	
	<K,V> Map<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value);
	
	void deleteCache(String name);

	void deleteClusteredCache(String name);
}
