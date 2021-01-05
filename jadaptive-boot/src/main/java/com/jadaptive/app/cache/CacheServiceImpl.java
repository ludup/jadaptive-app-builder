package com.jadaptive.app.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import org.springframework.stereotype.Service;

import com.jadaptive.api.cache.CacheService;

@Service
public class CacheServiceImpl implements CacheService {

//	@Autowired
//	private CacheManager cacheManager;
	
	
	public <K,V> Cache<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value){
		return cache(name, key, value, baseConfiguration(key, value));
	}
	
	public <K,V> Cache<K, V> getCacheOrCreate(String name,Class<K> key, Class<V> value,Factory<? extends ExpiryPolicy> expiryPolicyFactory){
		return cache(name, key, value, ((MutableConfiguration<K, V>)baseConfiguration(key, value)).setExpiryPolicyFactory(expiryPolicyFactory));
	}
	
	public <K,V> Cache<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value){
		return null;
	}
	
	public CacheManager getCacheManager(){
		return null;
	}
	
	private <K,V> CompleteConfiguration<K, V> baseConfiguration(Class<K> key, Class<V> value){
		return new MutableConfiguration<K, V>().setReadThrough(true).setWriteThrough(true).setTypes(key, value);
	} 
	
	private <K,V> Cache<K, V> cache(String name, Class<K> key, Class<V> value, CompleteConfiguration<K, V> config){
//		Cache<K, V> cache = cacheManager.getCache(name,key,value);
//		return cache == null ? cacheManager.createCache(name, config) : cache;
		return null;
	}
	
	
	class JCache<K, V> implements Cache<K, V> {

		@Override
		public V get(K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<K, V> getAll(Set<? extends K> keys) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean containsKey(K key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void loadAll(Set<? extends K> keys, boolean replaceExistingValues,
				CompletionListener completionListener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void put(K key, V value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public V getAndPut(K key, V value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> map) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean putIfAbsent(K key, V value) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean remove(K key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean remove(K key, V oldValue) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public V getAndRemove(K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean replace(K key, V oldValue, V newValue) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean replace(K key, V value) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public V getAndReplace(K key, V value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void removeAll(Set<? extends K> keys) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeAll() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
				throws EntryProcessorException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys,
				EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CacheManager getCacheManager() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isClosed() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> clazz) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deregisterCacheEntryListener(
				CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
