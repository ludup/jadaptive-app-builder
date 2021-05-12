package com.jadaptive.app.db;

import java.util.Iterator;
import java.util.Objects;

import javax.cache.Cache;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.app.ApplicationServiceImpl;

public class CachingIterable<T extends UUIDEntity> implements Iterable<T> {

		static Logger log = LoggerFactory.getLogger(CachingIterable.class);
		
		Iterable<Document> iterator;
		Cache<String,T> cachedObjects;
		Cache<String,UUIDList> cachedUUIDs;
		String cacheName;
		UUIDList processedUUIDs = new UUIDList();
		int maximumCachedUUIDs = Integer.parseInt(System.getProperty("jadaptive.iteratorCache.maxUUIDs", "100"));
		
		public CachingIterable(
				Iterable<Document> iterator, 
				Cache<String,T> cachedObjects,
				Cache<String,UUIDList> cachedUUIDs,
				String cacheName) {
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
				obj = DocumentHelper.convertDocumentToObject(
						ApplicationServiceImpl.getInstance().getBean(ClassLoaderService.class)
							.resolveClass(doc.getString("_clz")), doc);
				cachedObjects.put(obj.getUuid(), obj);
				if(processedUUIDs.size() < maximumCachedUUIDs) {
					processedUUIDs.add(obj.getUuid());
				}
				
				if(!iterator.hasNext()) {
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