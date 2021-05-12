package com.jadaptive.app.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.expiry.ExpiryPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.CacheConfiguration;
import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.ClassLoaderService;

@Service
public class CacheServiceImpl implements CacheService {

	@Autowired
	private CacheManager cacheManager;	
	
	@Autowired
	private ClassLoaderService classService; 
	
	public <K,V> Cache<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value){
		return cache(name, key, value, baseConfiguration(key, value, null));
	}
	
	public <K,V> Cache<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value,Factory<? extends ExpiryPolicy> expiryPolicyFactory){
		return cache(name, key, value, baseConfiguration(key, value, expiryPolicyFactory));
	}
	
	public <K,V> Cache<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value){
		return cacheManager.getCache(name,key,value);
	}
	
	public CacheManager getCacheManager(){
		return cacheManager;
	}
	
	private <K,V> CacheConfiguration<K, V> baseConfiguration(Class<K> key, Class<V> value, Factory<? extends ExpiryPolicy> expiryPolicyFactory){
		return new CacheConfig<K, V>() {
		 
			private static final long serialVersionUID = -3382954533694840362L;
			{
				setClassLoader(classService.getClassLoader());
			}
			
		}.setReadThrough(true).setWriteThrough(true).setTypes(key, value).setExpiryPolicyFactory(expiryPolicyFactory);
	} 
	
	private <K,V> Cache<K, V> cache(String name, Class<K> key, Class<V> value, CompleteConfiguration<K, V> config){
		Cache<K, V> cache = cacheManager.getCache(name,key,value);
		return cache == null ? cacheManager.createCache(name, config) : cache;
	}
}
