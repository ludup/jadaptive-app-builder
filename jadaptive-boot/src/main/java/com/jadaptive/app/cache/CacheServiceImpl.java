package com.jadaptive.app.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.ClassLoaderService;

@Service
public class CacheServiceImpl implements CacheService {

	@Autowired
	private ClassLoaderService classLoaderService;
	
	public <K,V> Cache<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value){
		return cache(name, key, value, baseConfiguration(key, value));
	}
	
	public <K,V> Cache<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value,Factory<? extends ExpiryPolicy> expiryPolicyFactory){
		return cache(name, key, value, ((MutableConfiguration<K, V>)baseConfiguration(key, value)).setExpiryPolicyFactory(expiryPolicyFactory));
	}
	
	public <K,V> Cache<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value){
		CacheManager cacheManager = Caching.getCachingProvider(classLoaderService.getClassLoader()).getCacheManager();
		return cacheManager.getCache(name,key,value);
	}
	
	public CacheManager getCacheManager(){
		return Caching.getCachingProvider(classLoaderService.getClassLoader()).getCacheManager();
	}
	
	private <K,V> CompleteConfiguration<K, V> baseConfiguration(Class<K> key, Class<V> value){
		return new MutableConfiguration<K, V>().setReadThrough(true).setWriteThrough(true).setTypes(key, value);
	} 
	
	private <K,V> Cache<K, V> cache(String name, Class<K> key, Class<V> value, CompleteConfiguration<K, V> config){
		CacheManager cacheManager = Caching.getCachingProvider(classLoaderService.getClassLoader()).getCacheManager();
		Cache<K, V> cache = cacheManager.getCache(name,key,value);
		return cache == null ? cacheManager.createCache(name, config) : cache;
	}
	
}
