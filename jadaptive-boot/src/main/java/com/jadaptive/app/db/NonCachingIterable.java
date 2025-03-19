package com.jadaptive.app.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
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

		private static Logger log = LoggerFactory.getLogger(NonCachingIterable.class);
		
		private final Iterable<Document> iterator;
		private final Map<String,Class<?>> cachedTemplates = new HashMap<>();
		
		public NonCachingIterable(Class<T> clz, 
				Iterable<Document> iterator) {
			if(log.isDebugEnabled()) {
				log.debug("Started uncached iteration for {} ", clz.getSimpleName());
			}
			this.iterator = iterator;
		}

		@Override
		public Iterator<T> iterator() {
			return new ConvertingIterator(iterator.iterator());
		}
		
		private Class<?> resolveClassFromTemplate(String resourceKey) throws ClassNotFoundException {
			
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
			throw new ClassNotFoundException("No template class found for " + resourceKey);
		}
	
		class ConvertingIterator implements Iterator<T> {

			final Iterator<Document> iterator;
			T next;
			
			public ConvertingIterator(Iterator<Document> iterator) {
				this.iterator = iterator;
			}

			@Override
			public boolean hasNext() {
				checkNext();
				return next != null;
			}

			@Override
			public T next() {
				checkNext();
				if(next == null)
					throw new NoSuchElementException();
				else {
					try {
						return next;
					}
					finally {
						next = null;
					}
				}
			}
			
			private void checkNext() {
				if(next == null) {
					while(iterator.hasNext()) {
						var document = iterator.next();
						try {
							var clazz = resolveClassFromTemplate(document.getString("resourceKey"));
							next = DocumentHelper.convertDocumentToObject(
									clazz, document);
							return;
						}
						catch(ClassNotFoundException cnfe) {
							if(log.isDebugEnabled())
								log.warn("{}. This may be because it uses a Java class that no longer exists (e.g. an extension has been removed), or is not accessible in this scope. You may need to clean up your data. ", cnfe.getMessage(), cnfe);
							else
								log.warn("{}. This may be because it uses a Java class that no longer exists (e.g. an extension has been removed), or is not accessible in this scope. You may need to clean up your data.", cnfe.getMessage());
						}
					}
				}
			}
		}
	}