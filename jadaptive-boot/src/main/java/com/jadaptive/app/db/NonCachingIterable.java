package com.jadaptive.app.db;

import java.util.Iterator;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.app.ApplicationServiceImpl;

public class NonCachingIterable<T extends UUIDEntity> implements Iterable<T> {

		static Logger log = LoggerFactory.getLogger(NonCachingIterable.class);
		
		Iterable<Document> iterator;

		public NonCachingIterable(
				Iterable<Document> iterator) {
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
				Document doc = iterator.next();
				return  DocumentHelper.convertDocumentToObject(
						ApplicationServiceImpl.getInstance().getBean(ObjectService.class)
							.getTemplateClass(doc.getString("_clz")), doc);
			}
		}
	}