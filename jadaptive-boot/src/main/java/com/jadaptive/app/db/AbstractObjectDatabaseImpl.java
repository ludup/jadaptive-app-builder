package com.jadaptive.app.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.cache.Cache;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.AbstractObjectDatabase;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.utils.Utils;
import com.mongodb.MongoWriteException;

public abstract class AbstractObjectDatabaseImpl implements AbstractObjectDatabase {

	private static final String DEFAULT_ITERATOR = "default";

	static Logger log = LoggerFactory.getLogger(AbstractObjectDatabaseImpl.class);
	
	protected final DocumentDatabase db;
	
	@Autowired
	private CacheService cacheService; 
	
	@Autowired
	private TemplateService templateService;
	
	@Autowired
	private ClassLoaderService classService; 
	
	@Autowired
	private DocumentHelper documentHelper;
	
	protected AbstractObjectDatabaseImpl(DocumentDatabase db) {
		this.db = db;
	}
	
	protected String getCollectionName(Class<?> clz) {
		ObjectDefinition template = clz.getAnnotation(ObjectDefinition.class);
		while(template!=null && template.type() == ObjectType.OBJECT) {
			clz = clz.getSuperclass();
			template = clz.getAnnotation(ObjectDefinition.class);
		} 
		if(Objects.nonNull(template)) {
			ObjectTemplate t = templateService.get(template.resourceKey());
			return t.getCollectionKey();
		}
		ObjectTemplate t = templateService.get(StringUtils.uncapitalize(clz.getSimpleName()));
		return t.getCollectionKey();
	}
	
	protected ObjectTemplate getObjectTemplate(Class<?> clz) {
		ObjectDefinition template = clz.getAnnotation(ObjectDefinition.class);
		while(template!=null && template.type() == ObjectType.OBJECT) {
			clz = clz.getSuperclass();
			template = clz.getAnnotation(ObjectDefinition.class);
		} 
		if(Objects.nonNull(template)) {
			ObjectTemplate t = templateService.get(template.resourceKey());
			return t;
		}
		throw new ObjectException(String.format("Missing template for class %s", clz.getSimpleName()));
	}
	
	protected  <T extends UUIDEntity> Cache<String, T> getCache(Class<T> clz) {
		return cacheService.getCacheOrCreate(String.format("%s.uuidCache", clz.getName()), String.class, clz);
	}
	
	protected  <T extends UUIDEntity> Cache<String, UUIDList> getIteratorCache(Class<T> clz) {
		return cacheService.getCacheOrCreate(String.format("%s.iterator", clz.getName()), String.class, UUIDList.class);
	}
	
	protected  <T extends UUIDEntity> Cache<String,UUIDList> getIteratorCache(Class<T> clz, String cacheName) {
		return cacheService.getCacheOrCreate(String.format("%s.searchCache",
				clz.getName()), String.class, UUIDList.class);
	}

	@SuppressWarnings("rawtypes")
	protected <T extends UUIDEntity> Cache<T, List> getIteratorCache(String name, Class<T> clz) {
		return cacheService.getCacheOrCreate(String.format("iterator.%s.%s", clz.getSimpleName(), name), clz, List.class);
	}
	
	protected <T extends UUIDEntity> void saveObject(T obj,  String database) throws RepositoryException, ObjectException {
		saveObject(obj, templateService.get(obj.getResourceKey()), database);
	}
	
	protected <T extends UUIDEntity> void saveObject(T obj, ObjectTemplate template, String database) throws RepositoryException, ObjectException {
		try {

//			Document previous = null;
//			
//			if(StringUtils.isNotBlank(obj.getUuid())) {
//				try {
//					previous = db.getByUUID(obj.getUuid(), getCollectionName(obj.getClass()), database);
//				} catch(ObjectNotFoundException ex) {
//				}
//			}
			
			Document document = new Document();
			document.put("resourceKey", obj.getResourceKey());
			documentHelper.convertObjectToDocument(obj, document, template);
			
//			String contentHash = DocumentHelper.generateContentHash(templateRepository.get(obj.getResourceKey()), document);
//			document.put("contentHash", contentHash);
//			
//			if(Objects.nonNull(previous) && previous.getString("contentHash").equals(contentHash)) {
//				if(log.isDebugEnabled()) {
//					log.debug("Object {} with uuid {} has not been updated because it's new content hash is the same as the previous");
//				}
//				return;
//			}
			
			db.insertOrUpdate(document, getCollectionName(obj.getClass()), database);
			obj.setUuid(document.getString("_id"));
			
			if(Boolean.getBoolean("jadaptive.cache")) {
				/**
				 * Perform caching which will also inform us whether event
				 */
				@SuppressWarnings("unchecked")
				Cache<String,T> cachedObjects = getCache((Class<T>)obj.getClass());
				Thread.currentThread().setContextClassLoader(classService.getClassLoader());
				T prevObject = cachedObjects.getAndPut(obj.getUuid(), obj);
				onObjectUpdated(prevObject, obj);
			} else {
				/**
				 * TODO remove this and ensure previous object is returned, or create event fired.
				 */
				onObjectUpdated(null, obj);
			}
			
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: %s", obj.getClass().getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> T getObject(String uuid, String database, Class<T> clz) throws RepositoryException, ObjectException {
		try {
			
			if(Boolean.getBoolean("jadaptive.cache")) {
				Cache<String,T> cachedObjects = getCache(clz);
				T result = cachedObjects.get(uuid);
				if(Objects.nonNull(result)) {
					return result;
				}
				Document document = db.getByUUID(uuid, getCollectionName(clz), database);
				if(Objects.isNull(document)) {
					throw new ObjectNotFoundException(String.format("Object from %s not found with id %s", 
							getCollectionName(clz), uuid));
				}
				result = documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz));
				cachedObjects.put(result.getUuid(), result);
				return result;
			} else {
				Document document = db.getByUUID(uuid, getCollectionName(clz), database);
				if(Objects.isNull(document)) {
					throw new ObjectNotFoundException(String.format("Object from %s not found with id %s", 
							getCollectionName(clz), uuid));
				}
				return documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz));
			}
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> T getObject(String database, Class<T> clz, SearchField... fields) throws RepositoryException, ObjectException {
		try {

			ObjectTemplate template = templateService.getTemplateByClass(clz);
			if(Boolean.getBoolean("jadaptive.cache")) {
				
				Cache<String,T> cachedObjects = getCache(clz);
				String cacheName = getSearchFieldsText(fields, "AND");
				T result = cachedObjects.get(cacheName);
				if(Objects.nonNull(result)) {
					if(log.isInfoEnabled()) {
						log.info("Get CACHED {} results for search {}", clz.getSimpleName(), cacheName);
					}
					return result;
				}
				
				if(log.isInfoEnabled()) {
					log.info("Get UNCACHED {} results for search {}", clz.getSimpleName(), cacheName);
				}
				
				Document document = db.get(template.getCollectionKey(), database, fields);
				if(Objects.isNull(document)) {
					throw new ObjectNotFoundException(String.format("Object from %s not found for fields %s", 
							getCollectionName(clz),
							getSearchFieldsText(fields, "AND")));
				}
				
				result = documentHelper.convertDocumentToObject(document, template);
				cachedObjects.put(cacheName, result);
				return result;
			} else {
				Document document = db.get(template.getCollectionKey(), database, fields);
				if(Objects.isNull(document)) {
					throw new ObjectNotFoundException(String.format("Object from %s not found for fields %s", 
							getCollectionName(clz),
							getSearchFieldsText(fields, "AND")));
				}
				
				return documentHelper.convertDocumentToObject(document, template);
			}
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}

	protected <T extends UUIDEntity> T max(String database, Class<T> clz, String field) throws RepositoryException, ObjectException {
		try {
			
			Document document = db.max(getCollectionName(clz), database, field);
			if(Objects.isNull(document)) {
				throw new ObjectNotFoundException(String.format("Maximum value from %s not found for fields %s", 
						getCollectionName(clz),
						field));
			}
			
			if(Boolean.getBoolean("jadaptive.cache")) {
				String uuid = document.getString("_id");
				Cache<String,T> cachedObjects = getCache(clz);
				T result = cachedObjects.get(uuid);
				if(Objects.nonNull(result)) {
					return result;
				}
				
				result = documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz));
				cachedObjects.put(result.getUuid(), result);
				return result;
			} else {
				return  documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz));
			}
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	


	protected <T extends UUIDEntity> T min(String database, Class<T> clz, String field) throws RepositoryException, ObjectException {
		try {
			Document document = db.min(getCollectionName(clz), database, field);
			if(Objects.isNull(document)) {
				throw new ObjectNotFoundException(String.format("Minimum value from %s not found for fields %s", 
						getCollectionName(clz),
						field));
			}
			
			if(Boolean.getBoolean("jadaptive.cache")) {
				String uuid = document.getString("_id");
				Cache<String,T> cachedObjects = getCache(clz);
				T result = cachedObjects.get(uuid);
				if(Objects.nonNull(result)) {
					return result;
				}
				
				result = documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz));
				cachedObjects.put(result.getUuid(), result);
				return result;
			} else {
				return documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz));
			}
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	private String getSearchFieldsText(SearchField[] fields, String condition) {
		
		StringBuffer buf = new StringBuffer();
		
		if(fields.length > 0) {
			
			for(int i=0;i<fields.length;i++) {
				SearchField field = fields[i];
				switch(field.getSearchType()) {
				case AND:
					buf.append("(");
					buf.append(getSearchFieldsText(field.getFields(), " AND "));
					buf.append(")");
					break;
				case OR:
					buf.append("(");
					buf.append(getSearchFieldsText(field.getFields(), " OR "));
					buf.append(")");
					break;
				case EQUALS:
				case LIKE:
					buf.append(field.getColumn());
					buf.append("=");
					buf.append(field.getValue()[0]);
					break;
				case IN:
					buf.append(field.getColumn());
					buf.append(" IN(");
					buf.append(Utils.csv(field.getValue()));
					buf.append(")");
					break;
				case NOT:
					buf.append(field.getColumn());
					buf.append(" IS NOT ");
					buf.append(field.getValue()[0]);
					break;
				}
				
				if(i < fields.length-1) {
					buf.append(condition);
				}
			}
		} else {
			buf.append("DEFAULT");
		}
		
		return buf.toString();
	}

	private void checkException(Throwable e) throws ObjectException {
		if(e instanceof ObjectException) {
			throw (ObjectException)e;
		}
		if(e instanceof RepositoryException) {
			throw (RepositoryException)e;
		}
		if(e instanceof MongoWriteException) {
			MongoWriteException mwe = (MongoWriteException)e;
			switch(mwe.getCode()) {
			case 11000:
				throw new RepositoryException("The object could not be created because of a unique constraint violation");
			default:
				throw new RepositoryException(mwe.getError().getMessage().split(":")[0]);
			}
			
		}
		log.error("Document error", e);
	}
	
	protected <T extends UUIDEntity> void deleteObject(T obj, String database) throws RepositoryException, ObjectException {
		try {
			if(obj.isSystem()) {
				throw new ObjectException(String.format("You cannot delete system objects from %s", getCollectionName(obj.getClass())));
			}
			db.deleteByUUID(obj.getUuid(), getCollectionName(obj.getClass()), database);
			
			if(Boolean.getBoolean("jadaptive.cache")) {
				@SuppressWarnings("unchecked")
				Cache<String,T> cachedObjects = getCache((Class<T>) obj.getClass());
				cachedObjects.remove(obj.getUuid());
			}
			onObjectDeleted(obj);
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", obj.getClass().getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> void onObjectDeleted(T obj) { }

	protected <T extends UUIDEntity> void onObjectCreated(T obj) { }
	
	protected <T extends UUIDEntity> void onObjectUpdated(T previousObject, T obj) { }
	
	protected <T extends UUIDEntity> Iterable<T> listObjects(String database, Class<T> clz) throws RepositoryException, ObjectException {
		
		try {
			
			if(Boolean.getBoolean("jadaptive.cache")) {
				Cache<String,UUIDList> cachedUUIDs = getIteratorCache(clz);
				Cache<String,T> cachedObjects = getCache(clz);
				
				List<String> uuids = cachedUUIDs.get(DEFAULT_ITERATOR);
				
				if(Objects.nonNull(uuids) && uuids.size() > 0) {
					if(log.isInfoEnabled()) {
						log.info("Listing CACHED {} results for search {}", clz.getSimpleName(), DEFAULT_ITERATOR);
					}
					return new CachedIterable<T>(clz, 
							cachedObjects,
							uuids);
				} else {
					if(log.isInfoEnabled()) {
						log.info("Listing UNCACHED {} results for search {}", clz.getSimpleName(), DEFAULT_ITERATOR);
					}
					return new CachingIterable<T>(
							db.list(getCollectionName(clz), database), 
							cachedObjects,
							cachedUUIDs,
							DEFAULT_ITERATOR,
							documentHelper);
				}
			} else {
				return new NonCachingIterable<T>( 
						db.list(getCollectionName(clz), database), documentHelper);
			}
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> Iterable<T> listObjects(String database, Class<T> clz, SearchField... fields) throws RepositoryException, ObjectException {
		
		try {
			if(Boolean.getBoolean("jadaptive.cache")) {
				String cacheName = getSearchFieldsText(fields, "AND");
				Cache<String,UUIDList> cachedUUIDs = getIteratorCache(clz);
				Cache<String,T> cachedObjects = getCache(clz);
				
				UUIDList uuids = cachedUUIDs.get(cacheName);
				if(Objects.nonNull(uuids) && uuids.size() > 0) {
					if(log.isInfoEnabled()) {
						log.info("Listing CACHED {} results for search {}", clz.getSimpleName(), cacheName);
					}
					return new CachedIterable<T>(clz, 
							cachedObjects,
							uuids);
				} else {
					if(log.isInfoEnabled()) {
						log.info("Listing UNCACHED {} results for search {}", clz.getSimpleName(), cacheName);
					}
					return new CachingIterable<T>( 
							db.list(getCollectionName(clz), database, fields), 
							cachedObjects,
							cachedUUIDs,
							cacheName,
							documentHelper);
				}
			} else {
				return new NonCachingIterable<T>( 
						db.list(getCollectionName(clz), database, fields), documentHelper);
			}
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}

	protected <T extends UUIDEntity> Collection<T> searchObjects(String database, Class<T> clz, SearchField... fields) throws RepositoryException, ObjectException {
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.search(getCollectionName(clz), database, fields)) {
				results.add(documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz)));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> Collection<T> searchTable(String database, Class<T> clz, int start, int length, SearchField... fields) throws RepositoryException, ObjectException {
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.searchTable(getCollectionName(clz), database, start, length, fields)) {
				results.add(documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz)));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> Long searchCount(String database, Class<T> clz, SearchField... fields) throws RepositoryException, ObjectException {
		
		try {
			return db.searchCount(getCollectionName(clz), database, fields);			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	

	protected <T extends UUIDEntity> Collection<T> tableObjects(String database, Class<T> clz, String searchField, String searchValue, int start, int length) throws RepositoryException, ObjectException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.table(getCollectionName(clz), searchField, searchValue, database, start, length)) {
				results.add(documentHelper.convertDocumentToObject(document, templateService.getTemplateByClass(clz)));
			}
			
			return results;

		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> Long countObjects(String database, Class<T> clz, SearchField... fields) throws RepositoryException, ObjectException {
		
		try {
			return db.count(getCollectionName(clz), database, fields);			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	
}
