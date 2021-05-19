package com.jadaptive.app.entity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectRepository;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.EventType;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.templates.JsonTemplateEnabledService;
import com.jadaptive.api.templates.SystemTemplates;
import com.jadaptive.app.db.DocumentHelper;

@Service
public class ObjectServiceImpl extends AuthenticatedService implements ObjectService, JsonTemplateEnabledService<MongoEntity> {

	@Autowired
	private ObjectRepository entityRepository;
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private ClassLoaderService classService; 
	
	@Autowired
	private ApplicationService appService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private TenantAwareObjectDatabase<?> objectDatabase;
	
	@Autowired
	private DocumentHelper documentHelper; 
	
	@Override
	public AbstractObject createNew(ObjectTemplate template) {
		return new MongoEntity(template.getResourceKey());
	}
	
	@Override
	public AbstractObject getSingleton(String resourceKey) throws RepositoryException, ObjectException {

		permissionService.assertRead(resourceKey);
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()!=ObjectType.SINGLETON) {
			throw new ObjectException(String.format("%s is not a singleton entity", resourceKey));
		}
		AbstractObject e;
		
		try {
			e = entityRepository.getById(template, resourceKey);
			if(!resourceKey.equals(e.getResourceKey())) {
				throw new IllegalStateException();
			}
			return e;
		} catch(ObjectNotFoundException ex) {
			e = new MongoEntity(resourceKey);
			e.setUuid(resourceKey);
			return e;
		}
 	}
	
	@Override
	public AbstractObject get(String resourceKey, String uuid) throws RepositoryException, ObjectException {

		permissionService.assertRead(resourceKey);
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()!=ObjectType.COLLECTION) {
			throw new ObjectException(String.format("%s is not a collection entity", resourceKey));
		}

		
		AbstractObject e = entityRepository.getById(template, uuid);
		if(!resourceKey.equals(e.getResourceKey()) && !template.getChildTemplates().contains(e.getResourceKey())) {
			throw new IllegalStateException(String.format("Unexpected template %s", e.getResourceKey()));
		}
		return e;
 	}


	@Override
	public Iterable<AbstractObject> list(String resourceKey) throws RepositoryException, ObjectException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getScope()==ObjectScope.PERSONAL) {
			throw new ObjectException(String.format("%s is a personal scoped object. Use personal API", resourceKey));
		}
		
		if(template.getPermissionProtected()) {
			permissionService.assertRead(resourceKey);
		}

		return entityRepository.list(template);
	}
	
	@Override
	public Collection<AbstractObject> personal(String resourceKey) throws RepositoryException, ObjectException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getScope()!=ObjectScope.PERSONAL) {
			throw new ObjectException(String.format("%s is a not personal scoped object. Use list API", resourceKey));
		}
		return entityRepository.personal(template, getCurrentUser());
	}

	@Override
	public void saveOrUpdate(AbstractObject entity) throws RepositoryException, ObjectException {
		
		permissionService.assertReadWrite(entity.getResourceKey());
		
		ObjectTemplate template = templateService.get(entity.getResourceKey());
		if(template.getType()==ObjectType.SINGLETON && !entity.getUuid().equals(entity.getResourceKey())) {	
			throw new ObjectException("You cannot save a Singleton Entity with a new UUID");
		}
		
		// Save
		if(Objects.nonNull(template.getTemplateClass())) {
			saveViaObjectBean(entity, template);
		} else {
			objectDatabase.saveOrUpdate(documentHelper.convertDocumentToObject(new Document(entity.getDocument())));
		
		}
	}

	

	private String saveViaObjectBean(AbstractObject entity, ObjectTemplate template) {
		
		permissionService.assertReadWrite(entity.getResourceKey());
		
		Class<? extends UUIDDocument> clz = classService.getTemplateClass(template);
		ObjectServiceBean annotation = clz.getAnnotation(ObjectServiceBean.class);
		if(Objects.nonNull(annotation)) {
			UUIDObjectService<?> bean = appService.getBean(annotation.bean());
			return bean.saveOrUpdate(documentHelper.convertDocumentToObject(new Document(entity.getDocument())));
		} else {
			return entityRepository.save(entity);
		}
	}

	@Override
	public void delete(String resourceKey, String uuid) throws RepositoryException, ObjectException {
		
		permissionService.assertReadWrite(resourceKey);
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()==ObjectType.SINGLETON) {	
			throw new ObjectException("You cannot delete a Singleton Entity");
		}
		
		AbstractObject e = get(resourceKey, uuid);
		if(e.isSystem()) {
			throw new ObjectException("You cannot delete a system object");
		}
		
		try {
			
			Class<? extends UUIDDocument> clz = classService.getTemplateClass(template);
			ObjectServiceBean annotation = clz.getAnnotation(ObjectServiceBean.class);
			if(Objects.nonNull(annotation)) {
				UUIDObjectService<?> bean = appService.getBean(annotation.bean());
				bean.deleteObject(documentHelper.convertDocumentToObject(new Document(e.getDocument())));
				
				eventService.publishStandardEvent(EventType.DELETE, 
						documentHelper.convertDocumentToObject(new Document(e.getDocument())));
				return;
			}
			 
			
			entityRepository.deleteByUUIDOrAltId(template, uuid);
			
			eventService.publishDocumentEvent(EventType.DELETE, e);
			
			
		} catch(RepositoryException | ObjectException ex) {
			throw ex;
		} catch(Throwable ex) {
			// 
			throw new ObjectException(ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteAll(String resourceKey) throws ObjectException {
		
		permissionService.assertReadWrite(resourceKey);
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()==ObjectType.SINGLETON) {	
			throw new ObjectException("You cannot delete a Singleton Entity");
		}
		
		entityRepository.deleteAll(template);
	}
	
	@Override
	public Integer getTemplateOrder() {
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
		return entityRepository.table(templateService.get(resourceKey), searchField, searchValue, offset, limit);
	}

	@Override
	public long count(String resourceKey) {
		return entityRepository.count(templateService.get(resourceKey));
	}
	
	@Override
	public long count(String resourceKey, String searchField, String searchValue) {
		return entityRepository.count(templateService.get(resourceKey), searchField, searchValue);
	}

}
