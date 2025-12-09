package com.jadaptive.app.cache;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.sshtools.gardensched.DistributedScheduledExecutor;

@Service
public class CacheServiceImpl extends AuthenticatedService implements CacheService {
	
	private final static Logger LOG = LoggerFactory.getLogger(CacheServiceImpl.class);

	private Map<String,Map<?,?>> caches = new HashMap<>();
	private Map<String,Map<?,?>> clusteredCaches = new HashMap<>();
	
	@Override
	public <K,V> Map<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value){
		return cache(name, key, value, 
			ApplicationProperties.getValue("cache." + name + ".expiry", 
				ApplicationProperties.getValue("cache.expiry", 60000 * 60 * 24)
			)
		); // One day
	}

	@Override
	public <K,V> Map<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value,long expiryTime){
		return cache(name, key, value, expiryTime);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K,V> Map<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value){
		return (Map<K, V>) caches.get(generateName(name));
	}

	@SuppressWarnings("unchecked")
	private <K,V> Map<K, V> cache(String name, Class<K> key, Class<V> value, long expiryTime){
		var cname = generateName(name);
		Map<K, V> cache = (Map<K, V>) caches.get(cname);
		if(cache==null) {

			cache = (Map<K, V>) Caffeine.newBuilder()
	            .expireAfterWrite(expiryTime, TimeUnit.MILLISECONDS)
	            .maximumSize(ApplicationProperties.getValue("cache." + name + ".size", ApplicationProperties.getValue("cache.size", 1000)))
	            .build()
	            .asMap();
			
			caches.put(cname, cache);
			
			LOG.info("Creating new cache {} [`{}`]. There are now {} caches.", name, cname, caches.size());
			
		}
		return cache;
	}
	
	@Override
	public <K,V> Map<K, V> clusteredCacheOrCreate(String name,Class<K> key, Class<V> value){
		return clusteredCache(name, key, value, 
			ApplicationProperties.getValue("clusteredCache." + name + ".expiry", 
				ApplicationProperties.getValue("clusteredCache.expiry", 60000 * 60 * 24)  // One day
			)
		);
	}

	@Override
	public <K,V> Map<K, V> clusteredCacheOrCreate(String name,Class<K> key, Class<V> value,long expiryTime){
		return clusteredCache(name, key, value, expiryTime);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K,V> Map<K, V> clusteredCacheIfExists(String name, Class<K> key, Class<V> value){
		return (Map<K, V>) clusteredCaches.get(generateName(name));
	}

	@SuppressWarnings("unchecked")
	private <K,V> Map<K, V> clusteredCache(String name, Class<K> key, Class<V> value, long expiryTime){
		var cname = generateName(name);
		Map<K, V> cache = (Map<K, V>) clusteredCaches.get(cname);
		if(cache==null) {

			var executor = ApplicationServiceImpl.getInstance().getBean( DistributedScheduledExecutor.class);

			cache = new AbstractMap<K, V>() {
				@Override
				public Set<Entry<K, V>> entrySet() {
					throw new UnsupportedOperationException();
				}

				@Override
				public V get(Object key) {
					return (V) executor.get(cname, (Serializable)key);
				}

				@Override
				public V put(K key, V value) {
					/* TODO find out if anybody cares about previous value and
					 * avoid this retrieval for no reason */
					var was = get(key);
					executor.put(cname, (Serializable)key, (Serializable)value);
					return was;
				}

				@Override
				public V remove(Object key) {
					/* TODO find out if anybody cares about previous value and
					 * avoid this retrieval for no reason */
					var was = get(key);
					if(was != null) {
						executor.remove(cname, (Serializable)key);
					}
					return was;
				}
			};
			
			clusteredCaches.put(cname, cache);
			
			LOG.info("Creating new clustered cache {} [`{}`]. There are now {} clustered caches.", name, cname, caches.size());
			
		}
		return cache;
	}

	private String generateName(String name) {
		return String.format("%s-%s", name, getCurrentTenant().getUuid());
	}
}
