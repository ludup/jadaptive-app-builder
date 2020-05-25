package com.jadaptive.app.entity;

import java.util.Collection;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectRepository;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.EventType;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.templates.SystemTemplates;
import com.jadaptive.api.templates.JsonTemplateEnabledService;
import com.jadaptive.app.db.DocumentHelper;
import com.jadaptive.app.db.SearchHelper;

@Service
public class ObjectServiceImpl extends AuthenticatedService implements ObjectService, JsonTemplateEnabledService<MongoEntity> {

	@Autowired
	private ObjectRepository entityRepository;
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private SearchHelper searchHelper;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private ClassLoaderService classService; 
	
	@Override
	public AbstractObject getSingleton(String resourceKey) throws RepositoryException, ObjectException {

		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()!=ObjectType.SINGLETON) {
			throw new ObjectException(String.format("%s is not a singleton entity", resourceKey));
		}
		AbstractObject e;
		
		try {
			e = entityRepository.get(resourceKey, resourceKey);
			if(!resourceKey.equals(e.getResourceKey())) {
				throw new IllegalStateException();
			}
			return e;
		} catch(ObjectNotFoundException ex) {
			e = new MongoEntity();
			e.setResourceKey(resourceKey);
			e.setUuid(resourceKey);
			return e;
		}
 	}
	
	@Override
	public AbstractObject get(String resourceKey, String uuid) throws RepositoryException, ObjectException {

		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()!=ObjectType.COLLECTION) {
			throw new ObjectException(String.format("%s is not a collection entity", resourceKey));
		}
		
		AbstractObject e = entityRepository.get(uuid, resourceKey);
		if(!resourceKey.equals(e.getResourceKey())) {
			throw new IllegalStateException();
		}
		return e;
 	}


	@Override
	public Collection<AbstractObject> list(String resourceKey) throws RepositoryException, ObjectException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		return entityRepository.list(resourceKey, searchHelper.parseFilterField(template.getDefaultFilter()));
	}

	@Override
	public String saveOrUpdate(AbstractObject entity) throws RepositoryException, ObjectException {
		ObjectTemplate template = templateService.get(entity.getResourceKey());
		if(template.getType()==ObjectType.SINGLETON && !entity.getUuid().equals(entity.getResourceKey())) {	
			throw new ObjectException("You cannot save a Singleton Entity with a new UUID");
		}
		
		return entityRepository.save(entity);
		
	}

	@Override
	public void delete(String resourceKey, String uuid) throws RepositoryException, ObjectException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()==ObjectType.SINGLETON) {	
			throw new ObjectException("You cannot delete a Singleton Entity");
		}
		
		AbstractObject e = get(resourceKey, uuid);
		if(e.isSystem()) {
			throw new ObjectException("You cannot delete a system object");
		}
		
		try {
			
			Class<?> clz = classService.findClass((String)e.getValue("_clz"));
			DocumentHelper.convertDocumentToObject(clz, new Document(e.getDocument()));
			
			entityRepository.delete(resourceKey, uuid);
			
			eventService.publishStandardEvent(EventType.DELETE, 
					DocumentHelper.convertDocumentToObject(clz, 
							new Document(e.getDocument())));
			
		} catch(RepositoryException | ObjectException ex) {
			throw ex;
		} catch(Throwable ex) {
			// 
			throw new ObjectException(ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteAll(String resourceKey) throws ObjectException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()==ObjectType.SINGLETON) {	
			throw new ObjectException("You cannot delete a Singleton Entity");
		}
		
		entityRepository.deleteAll(resourceKey);
		
	}
	
	@Override
	public Integer getWeight() {
		return SystemTemplates.ENTITY.ordinal();
	}

	@Override
	public Class<MongoEntity> getResourceClass() {
		return MongoEntity.class;
	}

	@Override
	public String getName() {
		return "Entity";
	}

	@Override
	public MongoEntity createEntity() {
		return new MongoEntity();
	}

	@Override
	public String getResourceKey() {
		return "entity";
	}

	@Override
	public void saveTemplateObjects(List<MongoEntity> objects, @SuppressWarnings("unchecked") TransactionAdapter<MongoEntity>... ops) throws RepositoryException, ObjectException {
		
		for(MongoEntity obj : objects) {
			saveOrUpdate(obj);
			for(TransactionAdapter<MongoEntity> op : ops) {
				op.afterSave(obj);
			}
		}
	}

	@Override
	public boolean isSystemOnly() {
		return false;
	}

	@Override
	public String getTemplateFolder() {
		return "objects";
	}

	@Override
	public Collection<AbstractObject> table(String resourceKey, String searchField, String searchValue, int offset, int limit) {
		templateService.get(resourceKey);
		return entityRepository.table(resourceKey, searchField, searchValue, offset, limit);
	}

	@Override
	public long count(String resourceKey) {
		templateService.get(resourceKey);
		return entityRepository.count(resourceKey);
	}
	
	@Override
	public long count(String resourceKey, String searchField, String searchValue) {
		templateService.get(resourceKey);
		return entityRepository.count(resourceKey, searchField, searchValue);
	}
}
