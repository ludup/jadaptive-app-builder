package com.jadaptive.app.db;

import java.text.ParseException;
import java.util.Iterator;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;

public class NonCachingIterable<T extends UUIDEntity> implements Iterable<T> {

		static Logger log = LoggerFactory.getLogger(NonCachingIterable.class);
		
		Iterable<Document> iterator;
		Class<T> clz;
		
		public NonCachingIterable(Class<T> clz, 
				Iterable<Document> iterator) {
			if(log.isInfoEnabled()) {
				log.info("Started uncached iteration for {} ", clz.getSimpleName());
			}
			this.clz = clz;
			this.iterator = iterator;
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
				try {
					Document doc = iterator.next();
					T obj = DocumentHelper.convertDocumentToObject(clz, doc);
					return obj;
				} catch (ParseException e) {
					throw new RepositoryException(e);
				}
			}
		}
	}