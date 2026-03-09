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
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.tenant.Tenant;

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
	public void deleteCache(String name) {
		var cname = generateName(name);
		synchronized (caches) {
			if(caches.remove(cname) != null) {
				LOG.info("Deleted cache {} [`{}`]. There are now {} caches.", name, cname, caches.size());
			}
			else {
				LOG.warn("Cache {} [`{}`] does not exist. No cache deleted.", name, cname);
			}
		}
		
	}

	@Override
	public void deleteClusteredCache(String name) {
		var cname = generateName(name);
		synchronized (clusteredCaches) {
			if(clusteredCaches.remove(cname) != null) {
				LOG.info("Deleted clustered cache {} [`{}`]. There are now {} caches.", name, cname, clusteredCaches.size());
			}
			else {
				LOG.warn("Clustered cache {} [`{}`] does not exist. No cache deleted.", name, cname);
			}
		}
		
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
		synchronized (caches) {
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
		synchronized (clusteredCaches) {
			Map<K, V> cache = (Map<K, V>) clusteredCaches.get(cname);
			if(cache==null) {
	
				var executor = ApplicationServiceImpl.getInstance().getBean( SchedulerService.class);
	
				cache = new AbstractMap<K, V>() {
					@Override
					public Set<Entry<K, V>> entrySet() {
						return keySet().stream()
							.map(k -> new SimpleEntry<>(k, get(k)))
							.collect(java.util.stream.Collectors.toSet());
					}
	
					@Override
					public Set<K> keySet() {
						return (Set<K>) executor.keySet(cname);
					}

					@Override
					public int size() {
						return executor.size(cname);
					}

					@Override
					public boolean containsKey(Object key) {
						return get(key) != null;
					}
	
					@Override
					public V get(Object key) {
						return (V) executor.get(cname, (Serializable)key);
					}
	
					@Override
					public V put(K key, V value) {
						if(value == null) {
							throw new IllegalArgumentException("Null values are not allowed in clustered caches.");
						}
						
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
	}

	private String generateName(String name) {
		Tenant ten = getCurrentTenant();
		return String.format("%s/%s", ten == null ? tenantService.getSystemTenant().getUuid() : ten.getUuid(), name);
	}
}
