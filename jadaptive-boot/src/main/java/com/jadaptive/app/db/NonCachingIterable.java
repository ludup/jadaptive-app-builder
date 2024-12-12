package com.jadaptive.app.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;

public class NonCachingIterable<T extends UUIDDocument> implements Iterable<T> {

		static Logger log = LoggerFactory.getLogger(NonCachingIterable.class);
		
		Iterable<Document> iterator;
		Class<T> clz;
		Map<String,Class<?>> cachedTemplates = new HashMap<>();
		
		public NonCachingIterable(Class<T> clz, 
				Iterable<Document> iterator) {
			if(log.isDebugEnabled()) {
				log.debug("Started uncached iteration for {} ", clz.getSimpleName());
			}
			this.clz = clz;
			this.iterator = iterator;
		}

		@Override
		public Iterator<T> iterator() {
			return new ConvertingIterator(iterator.iterator());
		}
		
		private Class<?> resolveClassFromTemplate(String resourceKey) {
			
			Class<?> clz = cachedTemplates.get(resourceKey);
			if(Objects.nonNull(clz)) {
				return clz;
			}
			ObjectTemplate template = ApplicationServiceImpl.getInstance().getBean(ObjectTemplateRepository.class).get(resourceKey);
			if(StringUtils.isNotBlank(template.getTemplateClass())) {
				ClassLoaderService classLoader = ApplicationServiceImpl.getInstance().getBean(ClassLoaderService.class);
				try {
					clz = classLoader.findClass(template.getTemplateClass());
					cachedTemplates.put(resourceKey, clz);
					return clz;
				} catch (ClassNotFoundException e) {
					if(StringUtils.isNotBlank(template.getClassDefinition())) {
						classLoader.injectClass(template);
						try {
							clz = classLoader.findClass(template.getTemplateClass());
							cachedTemplates.put(resourceKey, clz);
							return clz;
						} catch (ClassNotFoundException e2) {
							throw new IllegalStateException(e2.getMessage(), e2);
						}
					}
				}
				
			} 
			throw new IllegalStateException("No template class found for " + resourceKey);
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
						resolveClassFromTemplate(doc.getString("resourceKey")), doc);
			}
		}
	}