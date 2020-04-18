package com.jadaptive.api.cache;

import javax.cache.Cache;
import javax.cache.configuration.Factory;
import javax.cache.expiry.ExpiryPolicy;

public interface CacheService {

	public <K,V> Cache<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value);
	
	public <K,V> Cache<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value,Factory<? extends ExpiryPolicy> expiryPolicyFactory);
	
	public <K,V> Cache<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value);
}
