package com.jadaptive.app.db;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.repository.UUIDEntity;

public class CachingIterable<T extends UUIDEntity> implements Iterable<T> {

		static Logger log = LoggerFactory.getLogger(CachingIterable.class);
		
		Iterable<Document> iterator;
		Class<T> clz;
		Map<String,T> cachedObjects;
		Map<String,UUIDList> cachedUUIDs;
		String cacheName;
		UUIDList processedUUIDs = new UUIDList();
		int maximumCachedUUIDs = Integer.parseInt(System.getProperty("jadaptive.iteratorCache.maxUUIDs", "100"));
		
		public CachingIterable(Class<T> clz, 
				Iterable<Document> iterator, 
				Map<String,T> cachedObjects,
				Map<String,UUIDList> cachedUUIDs,
				String cacheName) {
			if(log.isInfoEnabled()) {
				log.info("Started cached iteration for {} ", clz.getSimpleName());
			}
			this.clz = clz;
			this.iterator = iterator;
			this.cachedObjects = cachedObjects;
			this.cachedUUIDs = cachedUUIDs;
			this.cacheName = cacheName;
		}

		@Override
		public Iterator<T> iterator() {
			return new ConvertingIterator(iterator.iterator());
		}
	
		class ConvertingIterator implements Iterator<T> {

			Iterator<Document> iterator;
			
			public ConvertingIterator(Iterator<Document> iterator) {
				this.iterator = iterator;
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {

				Document doc = iterator.next();
				String uuid = doc.getString("_id");
				T obj = cachedObjects.get(uuid);
				if(Objects.nonNull(obj)) {
					if(processedUUIDs.size() < maximumCachedUUIDs) {
						processedUUIDs.add(obj.getUuid());
					}
					return obj;
				}
				obj = DocumentHelper.convertDocumentToObject(clz, doc);
				cachedObjects.put(obj.getUuid(), obj);
				if(processedUUIDs.size() < maximumCachedUUIDs) {
					processedUUIDs.add(obj.getUuid());
				}
				
				if(!iterator.hasNext()) {
					if(log.isInfoEnabled()) {
						log.info("Finished uncached iteration for {} ", clz.getSimpleName());
					}
					/**
					 * We have reached end of the iterator. Should we
					 * cache the operation?
					 */
					if(processedUUIDs.size() <= maximumCachedUUIDs) {
						cachedUUIDs.put(cacheName, processedUUIDs);
					}
				}
				return obj;

			}
		}
	}