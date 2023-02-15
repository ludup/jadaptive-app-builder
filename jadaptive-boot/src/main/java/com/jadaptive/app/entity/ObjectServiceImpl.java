package com.jadaptive.app.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectRepository;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.repository.UUIDReference;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.templates.JsonTemplateEnabledService;
import com.jadaptive.api.templates.SystemTemplates;
import com.jadaptive.api.tenant.AbstractTenantAwareObjectDatabase;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.db.DocumentHelper;
import com.jadaptive.app.tenant.AbstractSystemObjectDatabaseImpl;
import com.jadaptive.app.tenant.AbstractTenantAwareObjectDatabaseImpl;
import com.jadaptive.utils.UUIDObjectUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;

@Service
public class ObjectServiceImpl extends AuthenticatedService implements ObjectService, JsonTemplateEnabledService<MongoEntity> {

	static Logger log = LoggerFactory.getLogger(ObjectServiceImpl.class);
	
	@Autowired
	private ObjectRepository objectRepository;
	
	@Autowired
	private ObjectTemplateRepository templateRepository; 
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ClassLoaderService classService; 
	
	@Autowired
	private ApplicationService appService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private DocumentDatabase documentDatabase;

	Map<String,FormHandler> formHandlers = new HashMap<>();
	
	@Override
	public AbstractObject createNew(ObjectTemplate template) {
		return new MongoEntity(template.getResourceKey());
	}
	
	@Override
	public AbstractObject getSingleton(String resourceKey) throws RepositoryException, ObjectException, ValidationException {
	
		ObjectTemplate template = templateService.get(resourceKey);
		
		if(template.getPermissionProtected()) {
			permissionService.assertRead(resourceKey);
		}
		
		if(template.getType()!=ObjectType.SINGLETON) {
			throw new ObjectException(String.format("%s is not a singleton entity", resourceKey));
		}
		AbstractObject e;
		
		try {
			e = objectRepository.getById(template, resourceKey);
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
	public AbstractObject get(String resourceKey, String uuid) throws RepositoryException, ObjectException, ValidationException {

		ObjectTemplate template = templateService.get(resourceKey);
		return get(template, uuid);
	}
	
	@Override
	public AbstractObject get(ObjectTemplate template, String uuid) throws RepositoryException, ObjectException, ValidationException {
		/**
		 * This creates a problem requiring normal users to have read for anything that's embedded in an object.
		 */
//		if(!template.isSystem() && template.getPermissionProtected()) {
//			permissionService.assertRead(resourceKey);
//		}
		
		AbstractObject e = objectRepository.getById(template, uuid);
//		if(!resourceKey.equals(e.getResourceKey()) && !template.getChildTemplates().contains(e.getResourceKey())) {
//			throw new IllegalStateException(String.format("Unexpected template %s", e.getResourceKey()));
//		}
		return e;
 	}


	@Override
	public Iterable<AbstractObject> list(String resourceKey) throws RepositoryException, ObjectException {
		
		ObjectTemplate template = templateService.get(resourceKey);
//		if(template.getScope()==ObjectScope.PERSONAL) {
//			throw new ObjectException(String.format("%s is a personal scoped object. Use personal API", resourceKey));
//		}
		
		if(template.getPermissionProtected()) {
			permissionService.assertRead(resourceKey);
		}

		return objectRepository.list(template);
	}
	
//	@Override
//	public Collection<AbstractObject> personal(String resourceKey) throws RepositoryException, ObjectException {
//		
//		ObjectTemplate template = templateService.get(resourceKey);
//		if(template.getScope()!=ObjectScope.PERSONAL) {
//			throw new ObjectException(String.format("%s is a not personal scoped object. Use list API", resourceKey));
//		}
//		return entityRepository.personal(template, getCurrentUser());
//	}

	@Override
	public String saveOrUpdate(AbstractObject entity) throws RepositoryException, ObjectException {
		
		
		ObjectTemplate template = templateService.get(entity.getResourceKey());
		
		if(template.getPermissionProtected()) {
			permissionService.assertReadWrite(entity.getResourceKey());
		}
		
		assertReferencesExist(template, entity);
		
		switch(template.getType()) {
		case SINGLETON:
			if(!entity.getUuid().equals(entity.getResourceKey())) {	
				throw new ObjectException("You cannot save a Singleton Entity with a new UUID");
			}
			break;
		default:
			break;
		}
		
		if(Objects.nonNull(template.getTemplateClass())) {
			return saveViaObjectBean(entity, template);
		} else {
			return objectRepository.save(entity);
		}
	}
	
	@Override
	public void assertForiegnReferences(ObjectTemplate template, String uuid) {
		
		while(template.hasParent()) {
			template = templateRepository.get(template.getParentTemplate());
		}
		
		for(ObjectTemplate reference : templateRepository.findReferences(template)) {
			if(log.isInfoEnabled()) {
				log.info("Found foreign reference to {} in {}", template.getResourceKey(),
						reference.getResourceKey());
			}
		}
		for(ObjectTemplate reference : templateRepository.findReferences(template)) {
			validateCollectionReference(reference, template.getResourceKey(), uuid, "");
			
		}
	}
	
	@Override
	public void rebuildReferences(ObjectTemplate template) {
		
		if(templateRepository.hasReferences(template)) {
			log.info("Rebuilding references for {}", template.getResourceKey());
			for(AbstractObject obj : list(template.getResourceKey())) {
				saveOrUpdate(obj);
			}
		}
	}

	private void validateCollectionReference(ObjectTemplate reference, String foreignType, String foreignKey, String parentField) {
		for(FieldTemplate field : reference.getFields()) {
			switch(field.getFieldType())  {
			case OBJECT_REFERENCE:
			{
				String resourceKey = field.getValidationValue(ValidationType.RESOURCE_KEY);
				if(resourceKey.equals(foreignType)) {
					if(count(reference.getResourceKey(), generateFieldName(parentField, field), foreignKey) > 0) {
						throw new ObjectException(String.format("Cannot delete this object because it is still referenced by %s",
								reference.getResourceKey()));
					}
//					validateEventReference(reference, foreignType, foreignKey, generateFieldName(parentField, field));
				}
				break;
			}
			case OBJECT_EMBEDDED:
			{
				validateCollectionReference(templateRepository.get(field.getValidationValue(ValidationType.RESOURCE_KEY)), 
						foreignType, foreignKey, generateFieldName(parentField, field));
				break;
			}
			default:
			{
				break;
			}
			}
		}
	}
	
//	private void validateEventReference(ObjectTemplate reference, String foreignType, String foreignKey, String parentField) {
//		
//		if(objectRepository.count(templateService.get(SystemEvent.RESOURCE_KEY), 
//				SearchField.eq("object.resourceKey", reference.getResourceKey()),
//				SearchField.all(String.format("object.%s", parentField), foreignKey)) > 0) {
//			throw new ObjectException(String.format("Cannot delete this object because it is still referenced by events for %s",
//					reference.getResourceKey()));
//		}
//	}
	
	private void assertReferencesExist(ObjectTemplate template, AbstractObject entity) {
		validateReferences(template.getFields(), entity);
	}

	@SuppressWarnings("unchecked")
	private void validateReferences(Collection<FieldTemplate> fields, AbstractObject entity) {
		if(!Objects.isNull(fields)) {
			for(FieldTemplate t : fields) {
				switch(t.getFieldType()) {
				case OBJECT_REFERENCE:
					if(t.getCollection()) {
						Object tmp = entity.getValue(t.getResourceKey());
						if(tmp instanceof List) {
							Collection<?> values = (Collection<?>) entity.getValue(t.getResourceKey());
							if(Objects.nonNull(values)) {
								if(values.isEmpty()) {
									continue;
								}
								if(values.iterator().next() instanceof String) {
									for(String value : (Collection<String>)values) {
										validateEntityExists(value, templateRepository.get(t.getValidationValue(ValidationType.RESOURCE_KEY)));
									}
									continue;
								} 
							}
						} 
						
						Collection<AbstractObject> values = (Collection<AbstractObject>)entity.getObjectCollection(t.getResourceKey());
						if(Objects.nonNull(values)) {
							for(AbstractObject value : values) {
								validateEntityExists(value.getUuid(), templateRepository.get(t.getValidationValue(ValidationType.RESOURCE_KEY)));
							}
						}
						
					} else {
						Object tmp = entity.getValue(getName());
						if(tmp instanceof String) {
							if(StringUtils.isNotBlank(tmp.toString())) {
								validateEntityExists(tmp.toString(), templateRepository.get(t.getValidationValue(ValidationType.RESOURCE_KEY)));
							}
						} else {
							AbstractObject ref = entity.getChild(t);
							if(Objects.isNull(ref)) {
								continue;
							}
							if(StringUtils.isNotBlank(ref.getUuid())) {
								validateEntityExists(ref.getUuid(), templateRepository.get(t.getValidationValue(ValidationType.RESOURCE_KEY)));
							}
						}
					}
					break;
				case OBJECT_EMBEDDED:
					AbstractObject child = entity.getChild(t);
					if(Objects.nonNull(child)) {
						assertReferencesExist(templateRepository.get(
								t.getValidationValue(ValidationType.RESOURCE_KEY)), 
								child);
					}
					break;
				default:
					break;
				}
			}
		}
	}
	
	private void validateEntityExists(String uuid, ObjectTemplate def) {
		get(def, uuid);
	}
		
	private String generateFieldName(String parent, FieldTemplate field) {
		if(StringUtils.isBlank(parent)) {
			return field.getResourceKey();
		} else {
			return parent + "." + field.getResourceKey();
		}
	}

	private String saveViaObjectBean(AbstractObject entity, ObjectTemplate template) {
		
		if(template.getPermissionProtected()) {
			permissionService.assertReadWrite(entity.getResourceKey());
		}
		Class<? extends UUIDDocument> clz = classService.getTemplateClass(template);

		ObjectServiceBean annotation = ReflectionUtils.getAnnotation(clz, ObjectServiceBean.class);
		
		if(Objects.nonNull(annotation)) {
			UUIDObjectService<?> bean = appService.getBean(annotation.bean());
			return bean.saveOrUpdate(DocumentHelper.convertDocumentToObject(clz, new Document(entity.getDocument())));
		} else { 
			AbstractTenantAwareObjectDatabase<?> db = createService(clz, template);
			return db.saveOrUpdate(DocumentHelper.convertDocumentToObject(clz, new Document(entity.getDocument())));
		}
	}
	
	private void deleteViaObjectBean(AbstractObject entity, ObjectTemplate template) {
		
		if(template.getPermissionProtected()) {
			permissionService.assertReadWrite(entity.getResourceKey());
		}
		Class<? extends UUIDDocument> clz = classService.getTemplateClass(template);

		ObjectServiceBean annotation = ReflectionUtils.getAnnotation(clz, ObjectServiceBean.class);
		
		if(Objects.nonNull(annotation)) {
			UUIDObjectService<?> bean = appService.getBean(annotation.bean());
			bean.deleteObject(DocumentHelper.convertDocumentToObject(clz, new Document(entity.getDocument())));
		} else { 
			AbstractTenantAwareObjectDatabase<?> db = createService(clz, template);
			db.delete(DocumentHelper.convertDocumentToObject(clz, new Document(entity.getDocument())));
		}
	}
	
	private AbstractTenantAwareObjectDatabase<?> createService(Class<? extends UUIDDocument> clz, ObjectTemplate template) {
	
		if(template.isSystem()) {
			return createSystemService(clz);
		} else {
			return createTenantAwareService(clz);
		}
	}
	
	private AbstractTenantAwareObjectDatabase<?> createSystemService(Class<? extends UUIDDocument> clz) {
		try {
			Generic genericType = TypeDescription.Generic.Builder.parameterizedType(AbstractSystemObjectDatabaseImpl.class, clz).build();
			Generic resourceClass = TypeDescription.Generic.Builder.parameterizedType(Class.class, clz).build();
			Class<?> subclass = new ByteBuddy()
					  .subclass(genericType, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
					  .defineMethod("getResourceClass", resourceClass).intercept(FixedValue.value(clz))
					  .make()
					  .load(clz.getClassLoader())
					  .getLoaded();
			AbstractTenantAwareObjectDatabase<?> obj = (AbstractTenantAwareObjectDatabase<?>) 
					subclass.getConstructor(DocumentDatabase.class).newInstance(documentDatabase);
			appService.autowire(obj);
			return obj;
		} catch (Throwable e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	private AbstractTenantAwareObjectDatabase<?> createTenantAwareService(Class<? extends UUIDDocument> clz) {
		try {
			Generic genericType = TypeDescription.Generic.Builder.parameterizedType(AbstractTenantAwareObjectDatabaseImpl.class, clz).build();
			Class<?> subclass = new ByteBuddy()
					  .subclass(genericType, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
					  .make()
					  .load(clz.getClassLoader())
					  .getLoaded();
			AbstractTenantAwareObjectDatabase<?> obj = (AbstractTenantAwareObjectDatabase<?>) 
					subclass.getConstructor(DocumentDatabase.class).newInstance(documentDatabase);
			appService.autowire(obj);
			return obj;
		} catch (Throwable e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(String resourceKey, String uuid) throws RepositoryException, ObjectException {
		
		ObjectTemplate template = templateService.get(resourceKey);
		
		if(template.getType()==ObjectType.SINGLETON) {	
			throw new ObjectException("You cannot delete a Singleton Entity");
		}
		
		assertForiegnReferences(template, uuid);
		
		AbstractObject e = get(resourceKey, uuid);
		if(e.isSystem()) {
			throw new ObjectException("You cannot delete a system object");
		}
		
		if(Objects.nonNull(template.getTemplateClass())) {
			deleteViaObjectBean(e, template);
		} else {
			objectRepository.delete(e);
		}
		
	}

	@Override
	public void deleteAll(String resourceKey) throws ObjectException {
		
		permissionService.assertReadWrite(resourceKey);
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()==ObjectType.SINGLETON) {	
			throw new ObjectException("You cannot delete a Singleton Entity");
		}
		
		objectRepository.deleteAll(template);
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
		ObjectTemplate template = templateService.get(resourceKey);

		switch(template.getScope()) {
		case PERSONAL:
			return objectRepository.table(template, offset, limit,
						generateSearchFields(searchField, searchValue, template, SearchField.eq("ownerUUID", getCurrentUser().getUuid())));				
		case ASSIGNED:
			if(permissionService.isAdministrator(getCurrentUser())) {
				return objectRepository.table(template, offset, limit, 
						generateSearchFields(searchField, searchValue, template));
			}
			Collection<Role> userRoles = roleService.getRolesByUser(getCurrentUser());
			return objectRepository.table(template, offset, limit, 
					generateSearchFields(searchField, searchValue, template, SearchField.or(
							SearchField.all("users.uuid", getCurrentUser().getUuid()),
							SearchField.in("roles.uuid", UUIDObjectUtils.getUUIDs(userRoles)))));			
		case GLOBAL:
		default:
			return objectRepository.table(template, offset, limit, 
					generateSearchFields(searchField, searchValue, template));
		}
		
	}

	private SearchField[] generateSearchFields(String searchField, String searchValue, ObjectTemplate template, SearchField... additional) {
		List<SearchField> fields = new ArrayList<>();
		if(StringUtils.isNotBlank(searchValue)) {
			FieldTemplate f = template.getField(searchField);
			if(Objects.nonNull(f)) {
				switch(f.getFieldType()) {
				case OBJECT_REFERENCE:
					if(f.getCollection()) {
						fields.add(SearchField.all(searchField, searchValue));
					} else {
						fields.add(SearchField.eq(searchField, searchValue));
					}
					break;
				default:
					fields.add(SearchField.like(searchField, searchValue));
					break;
				}
			}
		}
		fields.addAll(Arrays.asList(additional));
		return fields.toArray(new SearchField[0]);
	}
	
	@Override
	public long count(String resourceKey) {
		ObjectTemplate template = templateService.get(resourceKey);
		
		switch(template.getScope()) {
		case PERSONAL:
			return objectRepository.count(template,
					SearchField.eq("ownerUUID", getCurrentUser().getUuid()));
		case ASSIGNED:
			if(permissionService.isAdministrator(getCurrentUser())) {
				return objectRepository.count(template);
			}
			Collection<Role> userRoles = roleService.getRolesByUser(getCurrentUser());
			return objectRepository.count(template,
					SearchField.or(
							SearchField.all("users.uuid", getCurrentUser().getUuid()),
							SearchField.in("roles.uuid", UUIDObjectUtils.getUUIDs(userRoles))
					));			
		case GLOBAL:
		default:
			return objectRepository.count(template);
		}
		
		
	}
	
	@Override
	public long count(String resourceKey, String searchField, String searchValue) {
		
		ObjectTemplate template = templateService.get(resourceKey);
		
		try {
			
			permissionService.assertRead(template.getResourceKey());
			return objectRepository.count(template, generateSearchFields(searchField, searchValue, template));
		
		} catch(AccessDeniedException e) {
			switch(template.getScope()) {
			case PERSONAL:
				return objectRepository.count(template,
						generateSearchFields(searchField, searchValue, template, SearchField.eq("ownerUUID", getCurrentUser().getUuid())));
			case ASSIGNED:
				Collection<Role> userRoles = roleService.getRolesByUser(getCurrentUser());
				return objectRepository.count(template,
						generateSearchFields(searchField, searchValue, template, SearchField.or(
								SearchField.all("users.uuid", getCurrentUser().getUuid()),
								SearchField.in("roles.uuid", UUIDObjectUtils.getUUIDs(userRoles)))));			
			case GLOBAL:
			default:
				return objectRepository.count(template, generateSearchFields(searchField, searchValue, template));
			}
		}
	}

	private void buildFormHandlers() {
		if(formHandlers.isEmpty()) {
			for(FormHandler handler : appService.getBeans(FormHandler.class)) {
				formHandlers.put(handler.getResourceKey(), handler);
			}
		}
	}
	
	@Override
	public FormHandler getFormHandler(String handler) {
		buildFormHandlers();
		
		FormHandler objectHandler = formHandlers.get(handler);
		if(Objects.isNull(objectHandler)) {
			throw new IllegalStateException(String.format("%s is not a known form handler", handler));
		}
		return objectHandler;
	}

	@Override
	public AbstractObject convert(UUIDEntity obj) {

		Document doc = new Document();
		DocumentHelper.convertObjectToDocument(obj, doc);		
		return new MongoEntity(obj.getResourceKey(), doc);
	}
	
	
}
