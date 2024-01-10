package com.jadaptive.app.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.AbstractObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.db.Transactional;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.Events;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.events.ObjectUpdateEvent;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.repository.UUIDEvent;
import com.jadaptive.api.template.ObjectCache;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.templates.TemplateUtils;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.utils.Utils;
import com.mongodb.MongoWriteException;

public abstract class AbstractObjectDatabaseImpl implements AbstractObjectDatabase {

	private static final String DEFAULT_ITERATOR = "default";

	static Logger log = LoggerFactory.getLogger(AbstractObjectDatabaseImpl.class);
	
	protected final DocumentDatabase db;
	
	@Autowired
	private CacheService cacheService; 
	
	@Autowired
	private ObjectTemplateRepository templateRepository;
	
	@Autowired
	private EventService eventService; 
	
	@Autowired
	private TemplateVersionService templateService;
	
	@Autowired
	private TransactionService transactionService; 
	
	protected AbstractObjectDatabaseImpl(DocumentDatabase db) {
		this.db = db;
	}
	
	protected boolean isCaching(Class<?> clz) {
		return clz.getAnnotation(ObjectCache.class) != null;
	}
	
	protected String getCollectionName(Class<?> clz) {
		
		ObjectDefinition template = clz.getAnnotation(ObjectDefinition.class);
		
		while(template==null || template.type() == ObjectType.OBJECT) {
			clz = clz.getSuperclass();
			template = clz.getAnnotation(ObjectDefinition.class);
		} 
		if(Objects.nonNull(template)) {
			String resourceKey = template.resourceKey();
			if(StringUtils.isBlank(resourceKey)) {
				resourceKey = TemplateUtils.lookupClassResourceKey(clz);
			}
			return templateRepository.get(resourceKey).getCollectionKey();
		}
		throw new ObjectException(String.format("Missing template for class %s", clz.getSimpleName()));
	}
	
	protected ObjectTemplate getObjectTemplate(Class<?> clz) {
		
		ObjectDefinition template = clz.getAnnotation(ObjectDefinition.class);
		while(template!=null && template.type() == ObjectType.OBJECT) {
			clz = clz.getSuperclass();
			template = clz.getAnnotation(ObjectDefinition.class);
		} 
		if(Objects.nonNull(template)) {
			String resourceKey = template.resourceKey();
			if(StringUtils.isBlank(resourceKey)) {
				resourceKey = TemplateUtils.lookupClassResourceKey(clz);
			}
			return templateRepository.get(resourceKey);
		}
		throw new ObjectException(String.format("Missing template for class %s", clz.getSimpleName()));
	}
	
	protected  <T extends UUIDEntity> Map<String, T> getCache(Class<T> clz) {
		return cacheService.getCacheOrCreate(String.format("%s.uuidCache", clz.getName()), String.class, clz);
	}
	
	protected  <T extends UUIDEntity> Map<String, UUIDList> getIteratorCache(Class<T> clz) {
		return cacheService.getCacheOrCreate(String.format("%s.iterator", clz.getName()), String.class, UUIDList.class);
	}
	
	protected  <T extends UUIDEntity> Map<String,UUIDList> getIteratorCache(Class<T> clz, String cacheName) {
		return cacheService.getCacheOrCreate(String.format("%s.searchCache",
				clz.getName()), String.class, UUIDList.class);
	}

//	@SuppressWarnings("rawtypes")
//	protected <T extends UUIDEntity> Map<Class<T>, List> getIteratorCache(String name, Class<T> clz) {
//		return cacheService.getCacheOrCreate(String.format("iterator.%s.%s", clz.getSimpleName(), name), clz, List.class);
//	}
	
	protected <T extends UUIDEntity> void stash(T object) {
		onObjectStashed(object);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends UUIDEntity> void saveObject(T obj, String database) throws RepositoryException, ObjectException {
		
		T previous = null;
		boolean isEvent = obj instanceof UUIDEvent;
		
		try {
			
			if(!isEvent) {
				if(StringUtils.isNotBlank(obj.getUuid())) {
					try {
						previous = getObject(obj.getUuid(), database, (Class<T>) obj.getClass());
						onObjectUpdating(obj, previous);
					} catch(ObjectNotFoundException e) {
					}
				}
				if(Objects.isNull(previous)) {
					onObjectCreating(obj);
				}
			}
			
			if(!transactionService.isTransactionActive()
					&& ReflectionUtils.hasAnnotationRecursive(obj.getClass(), Transactional.class)) {
				final T previousObject = previous;
				transactionService.executeTransaction(()->{
					doSave(obj, database, previousObject, isEvent);
				});	
			} else {
				doSave(obj, database, previous, isEvent);
			}
			
		} catch(Throwable e) {
			log.error("Failed to save object", e);
			if(!isEvent && !(obj instanceof ObjectTemplate)) {
				if(Objects.isNull(previous)) {
					onCreatedError(obj, e);
				} else {
					onUpdateError(obj, previous, e);
				}
			}
			checkException(e);
			throw new RepositoryException(String.format("%s: %s", obj.getClass().getSimpleName(), e.getMessage()), e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends UUIDEntity> void doSave(T obj, String database, T previous, boolean isEvent) {
		
		if(obj instanceof  AbstractUUIDEntity) {
			
			Date now = Utils.now();
			AbstractUUIDEntity e = (AbstractUUIDEntity)obj;
			if(Objects.isNull(previous) || Objects.isNull(e.getCreated())) {
				e.setCreated(now);
			}
			e.setLastModified(now);
		}
		
		Document document = new Document();
		document.put("resourceKey", obj.getResourceKey());
		DocumentHelper.convertObjectToDocument(obj, document);
		
//		String contentHash = DocumentHelper.generateContentHash(templateRepository.get(obj.getResourceKey()), document);
//		document.put("contentHash", contentHash);
		
//		if(Objects.nonNull(previous) && previous.getString("contentHash").equals(contentHash)) {
//			if(log.isDebugEnabled()) {
//				log.debug("Object {} with uuid {} has not been updated because it's new content hash is the same as the previous");
//			}
//			return;
//		}
		try {
			db.insertOrUpdate(document, getCollectionName(obj.getClass()), database);
			obj.setUuid(document.getString("_id"));
		
			if(!isEvent && !(obj instanceof ObjectTemplate)) {
			
				if(isCaching(obj.getClass())) {
					Map<String,T> cachedObjects = getCache((Class<T>)obj.getClass());
					if(log.isDebugEnabled()) {
						log.debug("CACHE: Saving database value and caching {}", obj.getClass().getSimpleName());
					}
					cachedObjects.put(obj.getUuid(), obj);
					if(Objects.isNull(previous)) {
						if(log.isDebugEnabled()) {
							log.debug("CACHE: New object added to {} collection so clearing iterator cache", obj.getClass().getSimpleName());
						}
						Map<String,UUIDList> cachedUUIDs = getIteratorCache(obj.getClass());
						cachedUUIDs.clear();
					}
				}
				
				if(Objects.isNull(previous)) {
					onObjectCreated(obj);
				} else {
					onObjectUpdated(obj, previous);
				}
			}
		} catch(Throwable e) {
			checkException(e);
			log.error("Captured error in object event", e);
		}

	}
	
	protected <T extends UUIDEntity> T getObject(String uuid, String database, Class<T> clz) throws RepositoryException, ObjectException {
		try {
			
			if(Objects.nonNull(uuid) && isCaching(clz)) {
				Map<String,T> cachedObjects = getCache(clz);
				T result = cachedObjects.get(uuid);
				if(Objects.nonNull(result)) {
					if(log.isDebugEnabled()) {
						log.debug("CACHE: Returning cached {} for uuid {}", clz.getSimpleName(), uuid);
					}
					return result;
				}
				Document document = db.getByUUID(uuid, getCollectionName(clz), database);
				if(Objects.isNull(document)) {
					throw new ObjectNotFoundException(String.format("Object from %s not found with id %s", 
							getCollectionName(clz), uuid));
				}
				result = DocumentHelper.convertDocumentToObject(clz, document);
				if(log.isDebugEnabled()) {
					log.debug("CACHE: Caching {} for uuid {}", clz.getSimpleName(), result.getUuid());
				}
				cachedObjects.put(result.getUuid(), result);
				return result;
			} else {
				Document document = db.getByUUID(uuid, getCollectionName(clz), database);
				if(Objects.isNull(document)) {
					throw new ObjectNotFoundException(String.format("Object from %s not found with id %s", 
							getCollectionName(clz), uuid));
				}
				return DocumentHelper.convertDocumentToObject(clz, document);
			}
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> T getObject(String database, Class<T> clz, SearchField... fields) throws RepositoryException, ObjectException {
		try {

			if(isCaching(clz)) {
				Map<String,T> cachedObjects = getCache(clz);
				String searchKey = createSearchKey(fields);
				if(log.isDebugEnabled()) {
					log.debug("CACHE: Looking for {} with search key {}", clz.getSimpleName(), searchKey);
				}
				T result = cachedObjects.get(searchKey);
				if(Objects.nonNull(result)) {
					if(log.isDebugEnabled()) {
						log.debug("CACHE: Returning value from {} cache", clz.getSimpleName());
					}
					return result;
				}
				Document document = db.get(getCollectionName(clz), database, fields);
				if(Objects.isNull(document)) {
					throw new ObjectNotFoundException(String.format("Object from %s not found for fields %s", 
							getCollectionName(clz),
							getSearchFieldsText(fields, "AND")));
				}
				
				result = DocumentHelper.convertDocumentToObject(clz, document);
				if(log.isDebugEnabled()) {
					log.debug("CACHE: Returning database value and caching {}", clz.getSimpleName());
				}
				cachedObjects.put(searchKey, result);
				return result;
			} else {
				Document document = db.get(getCollectionName(clz), database, fields);
				if(Objects.isNull(document)) {
					throw new ObjectNotFoundException(String.format("Object from %s not found for fields %s", 
							getCollectionName(clz),
							getSearchFieldsText(fields, "AND")));
				}
				
				return DocumentHelper.convertDocumentToObject(clz, document);
			}
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}

	private String createSearchKey(SearchField[] fields) {
		StringBuilder builder = new StringBuilder();
		iterateFields(fields, builder, SearchField.Type.AND);
		return builder.toString().trim();
		
	}
	
	private String iterateFields(SearchField[] fields, StringBuilder builder, SearchField.Type type) {
		
		int c = 0;
		for(SearchField field : fields) {
			
			if(c++ > 0) {
				builder.append(" ");
				builder.append(type.name());
			}
			
			switch(field.getSearchType()) {
			case OR:
			case AND:
				if(builder.length() > 0) {
					builder.append(" ");
					builder.append(field.getSearchType().name());
					builder.append(" ( ");
					iterateFields(field.getFields(), builder, field.getSearchType());
					builder.append(" )");
				} else {
					iterateFields(field.getFields(), builder, field.getSearchType());
				}
				break;
			default:
				builder.append(" ");
				builder.append(field.getColumn());
				builder.append(" ");
				builder.append(field.getSearchType().name());
				if(Objects.nonNull(field.getValue())) {
					boolean brackets = field.getSearchType() == SearchField.Type.IN || field.getSearchType() == SearchField.Type.ALL;
					if(brackets) {
						builder.append(" (");
					}
					Object[] values = field.getValue();
					if(values.length > 1) {
						
						builder.append(" ( ");
						builder.append(Utils.csv(field.getValue()));
						builder.append(" }");
					} else if(Objects.nonNull(values[0])) {
						builder.append(" ");
						builder.append(values[0].toString());
						builder.append(" ");
					
				    }
					if(brackets) {
						builder.append(" )");
					}
			}
			}
		}
		return builder.toString();
	}

	protected <T extends UUIDEntity> T max(String database, Class<T> clz, String field, SearchField... fields) throws RepositoryException, ObjectException {
		try {
			
			Document document = db.max(getCollectionName(clz), database, field, fields);
			if(Objects.isNull(document)) {
				throw new ObjectNotFoundException(String.format("Maximum value from %s not found for fields %s", 
						getCollectionName(clz),
						field));
			}
			
			if(isCaching(clz)) {
				String uuid = document.getString("_id");
				Map<String,T> cachedObjects = getCache(clz);
				T result = cachedObjects.get(uuid);
				if(Objects.nonNull(result)) {
					if(log.isDebugEnabled()) {
						log.debug("CACHE: Returning value from {} cache", clz.getSimpleName());
					}
					return result;
				}
				
				result = DocumentHelper.convertDocumentToObject(clz, document);
				if(log.isDebugEnabled()) {
					log.debug("CACHE: Returning database value and caching {} with uuid", clz.getSimpleName(), result.getUuid());
				}
				cachedObjects.put(result.getUuid(), result);
				return result;
			} else {
				return  DocumentHelper.convertDocumentToObject(clz, document);
			}
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	


	protected <T extends UUIDEntity> T min(String database, Class<T> clz, String field, SearchField... fields) throws RepositoryException, ObjectException {
		try {
			Document document = db.min(getCollectionName(clz), database, field, fields);
			if(Objects.isNull(document)) {
				throw new ObjectNotFoundException(String.format("Minimum value from %s not found for fields %s", 
						getCollectionName(clz),
						field));
			}
			
			if(isCaching(clz)) {
				String uuid = document.getString("_id");
				Map<String,T> cachedObjects = getCache(clz);
				T result = cachedObjects.get(uuid);
				if(Objects.nonNull(result)) {
					if(log.isDebugEnabled()) {
						log.debug("CACHE: Returning value from {} cache", clz.getSimpleName());
					}
					return result;
				}
				
				result = DocumentHelper.convertDocumentToObject(clz, document);
				if(log.isDebugEnabled()) {
					log.debug("CACHE: Returning database value and caching {} with uuid {}", clz.getSimpleName(), result.getUuid());
				}
				cachedObjects.put(result.getUuid(), result);
				return result;
			} else {
				return DocumentHelper.convertDocumentToObject(clz, document);
			}
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	private String getSearchFieldsText(SearchField[] fields, String condition) {
		
		StringBuffer buf = new StringBuffer();
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
			case ALL:
				buf.append(field.getColumn());
				buf.append(" ALL(");
				buf.append(Utils.csv(field.getValue()));
				buf.append(")");
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
			case GT:
				buf.append(field.getColumn());
				buf.append(">");
				buf.append(field.getValue());
				break;
			case GTE:
				buf.append(field.getColumn());
				buf.append(">=");
				buf.append(field.getValue());
				break;
			case LT:
				buf.append(field.getColumn());
				buf.append("<");
				buf.append(field.getValue());
				break;
			case LTE:
				buf.append(field.getColumn());
				buf.append("<=");
				buf.append(field.getValue());
				break;
			}
			
			if(i < fields.length-1) {
				buf.append(condition);
			}
		}
		
		if(log.isDebugEnabled()) {
			log.debug("Search {}", buf.toString());
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
		
		log.error("Document error", e);
		
		if(e instanceof MongoWriteException) {
			MongoWriteException mwe = (MongoWriteException)e;
			switch(mwe.getCode()) {
			case 11000:
			case -3:
				throw new RepositoryException("The object could not be saved because of a unique constraint violation", e);
			default:
				throw new RepositoryException(mwe.getError().getMessage().split(":")[0], e);
			}
			
		}
	}
	
	protected <T extends UUIDEntity> void deleteObject(T obj, String database) throws RepositoryException, ObjectException {
		
		if(obj instanceof AbstractUUIDEntity && ((AbstractUUIDEntity)obj).isSystem()) {
			throw new ObjectException(String.format("You cannot delete system objects from %s", getCollectionName(obj.getClass())));
		}
		
		try {
		
			onObjectDeleting(obj);
			
			db.deleteByUUID(obj.getUuid(), getCollectionName(obj.getClass()), database);
			
			if(isCaching(obj.getClass())) {
				@SuppressWarnings("unchecked")
				Map<String,T> cachedObjects = getCache((Class<T>) obj.getClass());
				if(log.isDebugEnabled()) {
					log.debug("CACHE: Deleting cached {} with uuid", obj.getClass().getSimpleName(), obj.getUuid());
				}
				cachedObjects.remove(obj.getUuid());
			}
			onObjectDeleted(obj);
		} catch(Throwable e) {
			onDeletingError(obj, e);
			checkException(e);
			throw new RepositoryException(String.format("%s: ", obj.getClass().getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> void delete(String database, Class<T> clz, SearchField... fields) throws RepositoryException, ObjectException {
			
		try {
		
			db.delete(database, getCollectionName(clz), fields);
			
			if(isCaching(clz)) {
				Map<String,T> cachedObjects = getCache(clz);
				if(log.isDebugEnabled()) {
					log.debug("CACHE: Ckearing cache {}", clz.getSimpleName());
				}
				cachedObjects.clear();
			}

		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> void onDeletingError(T obj, Throwable e) {
		
		fireEvent(Events.deleted(obj.getEventGroup()), obj, e);
	}

	protected <T extends UUIDEntity> void fireEvent(String eventKey, T obj, boolean ignoreErrors) {
		
		Class<? extends ObjectEvent<?>> eventClz = templateService.getEventClass(eventKey);
		
		if(Objects.nonNull(eventClz)) {
			try {
				eventService.publishEvent(createSuccessEvent(eventClz, obj));
			} catch(Throwable e) {
				log.error(e.getMessage(), e);
				if(!ignoreErrors) {
					throw e;
				}
			}
		}
	}
	
	protected <T extends UUIDEntity> void fireUpdateEvent(String eventKey, T obj, T previous, boolean ignoreErrors) {
		
		Class<? extends ObjectUpdateEvent<?>> eventClz = templateService.getUpdateEventClass(eventKey);
		
		if(Objects.nonNull(eventClz)) {
			try {
				eventService.publishEvent(createUpdateEvent(eventClz, obj, previous));
			} catch(Throwable e) {
				log.error(e.getMessage(), e);
				if(!ignoreErrors) {
					throw e;
				}
			}
		}
	}
	
	protected <T extends UUIDEntity> void fireEvent(String eventKey, T obj, Throwable t) {
		
		Class<? extends ObjectEvent<?>> eventClz = templateService.getEventClass(eventKey);
		
		if(Objects.nonNull(eventClz)) {
			try {
				eventService.publishEvent(createErrorEvent(eventClz, obj, t));
			} catch(Throwable e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	protected <T extends UUIDEntity> SystemEvent createUpdateEvent(Class<? extends ObjectUpdateEvent<?>> eventClz, T obj, T previous) {
		
		try {
			for(Constructor<?> c : eventClz.getConstructors()) {
				if(c.getParameterCount()==2) {
					if(c.getParameterTypes()[0].isAssignableFrom(obj.getClass())) {
						return (SystemEvent) c.newInstance(obj, previous);
					}
				}
			}
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		throw new IllegalStateException("No such constructor for event " + eventClz.getName() + " and object " + obj.getClass().getName());
	}

	protected <T extends UUIDEntity> SystemEvent createSuccessEvent(Class<? extends ObjectEvent<?>> eventClz, T obj) {
		
		try {
			for(Constructor<?> c : eventClz.getConstructors()) {
				if(c.getParameterCount()==1) {
					if(c.getParameterTypes()[0].isAssignableFrom(obj.getClass())) {
						return (SystemEvent) c.newInstance(obj);
					}
				}
			}
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		throw new IllegalStateException("No such constructor for event " + eventClz.getName() + " and object " + obj.getClass().getName());
	}
	
	protected <T extends UUIDEntity> SystemEvent createErrorEvent(Class<? extends ObjectEvent<?>> eventClz, T obj, Throwable t) {
		
		try {
			for(Constructor<?> c : eventClz.getConstructors()) {
				if(c.getParameterCount()==2) {
					if(c.getParameterTypes()[0].isAssignableFrom(obj.getClass())
							&& c.getParameterTypes()[1].isAssignableFrom(Throwable.class)) {
						return (SystemEvent) c.newInstance(obj, t);
					}
				}
			}
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		throw new IllegalStateException("No such constructor for event " + eventClz.getName() + " and object " + obj.getClass().getName());
	}

	protected <T extends UUIDEntity> void onObjectDeleted(T obj) {
		
		fireEvent(Events.deleted(obj.getEventGroup()), obj, true);
		
	}
	
	protected <T extends UUIDEntity> void onObjectDeleting(T obj) {
		
		fireEvent(Events.deleting(obj.getEventGroup()), obj, false);
		
	}
	
	protected <T extends UUIDEntity> void onObjectCreating(T obj) { 
		
		fireEvent(Events.creating(obj.getEventGroup()), obj, false);
	}

	protected <T extends UUIDEntity> void onObjectCreated(T obj) { 
		
		fireEvent(Events.created(obj.getEventGroup()), obj, true);
	}
	
	protected <T extends UUIDEntity> void onObjectStashed(T obj) { 
		
		fireEvent(Events.stashed(obj.getEventGroup()), obj, true);
	}

	protected <T extends UUIDEntity> void onCreatedError(T obj, Throwable t) { 
		
		fireEvent(Events.created(obj.getEventGroup()), obj, t);
	}
	
	protected <T extends UUIDEntity> void onUpdateError(T obj, T previous, Throwable t) { 
		
		fireEvent(Events.updated(obj.getEventGroup()), obj, t);
	}
	
	protected <T extends UUIDEntity> void onObjectUpdated(T obj, T previousObject) {
		
		fireUpdateEvent(Events.updated(obj.getEventGroup()), obj, previousObject, true);
	}
	
	protected <T extends UUIDEntity> void onObjectUpdating(T obj, T previousObject) {
		
		fireUpdateEvent(Events.updating(obj.getEventGroup()), obj, previousObject, false);
	}
	
	protected <T extends UUIDEntity> Iterable<T> listObjects(String database, Class<T> clz) throws RepositoryException, ObjectException {
		
		try {
			
			if(isCaching(clz)) {
				Map<String,UUIDList> cachedUUIDs = getIteratorCache(clz);
				Map<String,T> cachedObjects = getCache(clz);
				
				List<String> uuids = cachedUUIDs.get(DEFAULT_ITERATOR);
				
				if(Objects.nonNull(uuids) && uuids.size() > 0) {
					return new CachedIterable<T>(clz, 
							cachedObjects,
							uuids);
				} else {
					return new CachingIterable<T>(clz, 
							db.list(getCollectionName(clz), database), 
							cachedObjects,
							cachedUUIDs,
							DEFAULT_ITERATOR);
				}
			} else {
				return new NonCachingIterable<T>(clz, 
						db.list(getCollectionName(clz), database));
			}
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> Iterable<T> listObjects(String database, Class<T> clz, SearchField... fields) throws RepositoryException, ObjectException {
		
		try {
			if(isCaching(clz)) {
				String cacheName = getSearchFieldsText(fields, "AND");
				Map<String,UUIDList> cachedUUIDs = getIteratorCache(clz);
				Map<String,T> cachedObjects = getCache(clz);
				
				UUIDList uuids = cachedUUIDs.get(cacheName);
				if(Objects.nonNull(uuids) && uuids.size() > 0) {
					return new CachedIterable<T>(clz, 
							cachedObjects,
							uuids);
				} else {
					return new CachingIterable<T>(clz, 
							db.list(getCollectionName(clz), database, fields), 
							cachedObjects,
							cachedUUIDs,
							cacheName);
				}
			} else {
				return new NonCachingIterable<T>(clz, 
						db.list(getCollectionName(clz), database, fields));
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
				results.add(DocumentHelper.convertDocumentToObject(clz, document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: %s", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> Collection<T> searchObjects(String database, Class<T> clz, SortOrder order, String sortField, SearchField... fields) throws RepositoryException, ObjectException {
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.search(getCollectionName(clz), database, order, sortField, fields)) {
				results.add(DocumentHelper.convertDocumentToObject(clz, document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: %s", clz.getSimpleName(), e.getMessage()), e);
		}
	}
	
	protected <T extends UUIDEntity> Collection<T> searchTable(String database, Class<T> clz, int start, int length, SortOrder order, String sortField, SearchField... fields) throws RepositoryException, ObjectException {
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.searchTable(getCollectionName(clz), database, start, length, order, sortField, fields)) {
				try {
					results.add(DocumentHelper.convertDocumentToObject(clz, document));
				} catch (Throwable e) {
					log.error("Failed to create document from template", e);
				}
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
	

	protected <T extends UUIDEntity> Collection<T> tableObjects(String database, Class<T> clz, String searchField, String searchValue, int start, int length, SortOrder order, String sortField) throws RepositoryException, ObjectException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.table(getCollectionName(clz), searchField, searchValue, database, start, length, order, sortField)) {
				results.add(DocumentHelper.convertDocumentToObject(clz, document));
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
	
	
	protected <T extends UUIDEntity> Long sumLongValues(String database, Class<T> clz, String groupBy, SearchField... fields) throws RepositoryException, ObjectException {
		try {
			return db.sumLongValues(getCollectionName(clz), database, groupBy, fields);
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}		
	}
	
	protected <T extends UUIDEntity> Double sumDoubleValues(String database, Class<T> clz, String groupBy, SearchField... fields) throws RepositoryException, ObjectException {
		try {
			return db.sumDoubleValues(getCollectionName(clz), database, groupBy, fields);
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(String.format("%s: ", clz.getSimpleName(), e.getMessage()), e);
		}		
	}
	
}
