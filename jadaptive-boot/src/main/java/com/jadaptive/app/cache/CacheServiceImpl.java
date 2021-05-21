package com.jadaptive.app.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.jadaptive.api.cache.CacheService;

@Service
public class CacheServiceImpl implements CacheService {
	
	Map<String,Map<?,?>> caches = new HashMap<>();
	
	public <K,V> Map<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value){
		return cache(name, key, value, Long.MAX_VALUE);
	}
	
	public <K,V> Map<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value,long expiryTime){
		return cache(name, key, value, expiryTime);
	}
	
	public <K,V> Map<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value){
		return null;
	}
	
	private <K,V> Map<K, V> cache(String name, Class<K> key, Class<V> value, long exiryTime){
		@SuppressWarnings("unchecked")
		Map<K, V> cache = (Map<K, V>) caches.get(name);
		if(cache==null) {
			cache = new ExpiringConcurrentHashMap<K, V>(exiryTime);
			caches.put(name, cache);
		}
		return cache;
	}
	
	class ExpiringConcurrentHashMap<K,V> extends ConcurrentHashMap<K,V> {

		private static final long serialVersionUID = 4825825094828550762L;

		private Map<K, Long> entryTime = new ConcurrentHashMap<K, Long>();
		
	    private long expiryInMillis;
	    
	    public ExpiringConcurrentHashMap(long expiryInMillis) {
	    	this.expiryInMillis = expiryInMillis;
	    }

	    @Override
	    public V put(K key, V value) {
	        purgeEntries();
	        return doPut(key, value);
	    }

	    private V doPut(K key, V value) {
	    	Long date = entryTime.getOrDefault(key, new Long(System.currentTimeMillis()));
	        entryTime.put(key, date);
	        V returnVal = super.put(key, value);
	        return returnVal;
		}

		@Override
	    public void putAll(Map<? extends K, ? extends V> m) {
			purgeEntries();
	        for (K key : m.keySet()) {
	            doPut(key, m.get(key));
	        }
	    }

	    @Override
	    public V putIfAbsent(K key, V value) {
	    	purgeEntries();
	        if (!containsKey(key)) {
	            return doPut(key, value);
	        } else {
	            return get(key);
	        }
	    }
	    
	    @Override
		public V get(Object key) {
	    	purgeEntries();
			return super.get(key);
		}

		private void purgeEntries() {
	        long currentTime = new Date().getTime();
	        for (K key : entryTime.keySet()) {
	            if (currentTime > (entryTime.get(key) + expiryInMillis)) {
	                remove(key);
	                entryTime.remove(key);
	            }
	        }
	    }
	}
}
