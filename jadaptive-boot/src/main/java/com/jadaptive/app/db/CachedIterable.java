package com.jadaptive.app.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;

public class CachedIterable<T extends UUIDDocument> implements Iterable<T> {

	static Logger log = LoggerFactory.getLogger(CachedIterable.class);
	
		Class<T> clz;
		Map<String,T> cachedObjects;
		List<String> cachedUUIDs = new ArrayList<>();
		int maximumCachedUUIDs = 100;
		
		public CachedIterable(Class<T> clz, 
				Map<String,T> cachedObjects,
				List<String> cachedUUIDs) {
			if(log.isDebugEnabled()) {
				log.debug("Started cached iteration for {} ", clz.getSimpleName());
			}
			this.clz = clz;
			this.cachedObjects = cachedObjects;
			this.cachedUUIDs = cachedUUIDs;
		}

		@Override
		public Iterator<T> iterator() {
			return new ConvertingIterator(cachedUUIDs.iterator());
		}
	
		class ConvertingIterator implements Iterator<T> {

			Iterator<String> iterator;
			
			public ConvertingIterator(Iterator<String> iterator) {
				this.iterator = iterator;
			}

			@Override
			public boolean hasNext() {
				boolean next = iterator.hasNext();
				if(!next) {
					if(log.isDebugEnabled()) {
						log.debug("Finished cached iteration for {} ", clz.getSimpleName());
					}
				}
				return next;
			}

			@Override
			public T next() {
				String uuid = iterator.next();
				T obj = cachedObjects.get(uuid);
				if(Objects.nonNull(obj)) {
					return obj;
				}
				throw new RepositoryException("Cache appears to be out of sync");
			}
		}
	}