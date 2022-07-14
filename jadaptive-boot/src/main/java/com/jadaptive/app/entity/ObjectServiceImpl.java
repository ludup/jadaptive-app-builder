package com.jadaptive.app.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.Document;
import org.pf4j.util.StringUtils;
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
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectTemplate;
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

	@Autowired
	private ObjectRepository entityRepository;
	
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
	public AbstractObject get(String resourceKey, String uuid) throws RepositoryException, ObjectException, ValidationException {

		ObjectTemplate template = templateService.get(resourceKey);

		if(!template.isSystem() && template.getPermissionProtected()) {
			permissionService.assertRead(resourceKey);
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
		
		permissionService.assertReadWrite(entity.getResourceKey());
		
		ObjectTemplate template = templateService.get(entity.getResourceKey());
		
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
			return entityRepository.save(entity);
		}
	}

	private String saveViaObjectBean(AbstractObject entity, ObjectTemplate template) {
		
		permissionService.assertReadWrite(entity.getResourceKey());
		
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
		
		permissionService.assertReadWrite(resourceKey);
		
		ObjectTemplate template = templateService.get(resourceKey);
		if(template.getType()==ObjectType.SINGLETON) {	
			throw new ObjectException("You cannot delete a Singleton Entity");
		}
		
		try {
			
			if(classService.hasTemplateClass(template)) {
				Class<? extends UUIDDocument> clz = classService.getTemplateClass(template);
				ObjectServiceBean annotation = clz.getAnnotation(ObjectServiceBean.class);
				if(Objects.nonNull(annotation)) {
					UUIDObjectService<?> bean = appService.getBean(annotation.bean());
					
					UUIDDocument obj = bean.getObjectByUUID(uuid);
					if(obj.isSystem()) {
						throw new ObjectException("You cannot delete a system object");
					}
					bean.deleteObjectByUUID(uuid);
					return;
				}
			} 
			AbstractObject e = get(resourceKey, uuid);
			if(e.isSystem()) {
				throw new ObjectException("You cannot delete a system object");
			}
			entityRepository.deleteByUUIDOrAltId(template, uuid);

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
		ObjectTemplate template = templateService.get(resourceKey);
		
		switch(template.getScope()) {
		case PERSONAL:
			return entityRepository.table(template, offset, limit,
						generateSearchFields(searchField, searchValue, template, SearchField.eq("ownerUUID", getCurrentUser().getUuid())));				
		case ASSIGNED:
			Collection<Role> userRoles = roleService.getRolesByUser(getCurrentUser());
			return entityRepository.table(template, offset, limit, 
					generateSearchFields(searchField, searchValue, template, SearchField.or(
							SearchField.in("users", getCurrentUser().getUuid()),
							SearchField.in("roles", UUIDObjectUtils.getUUIDs(userRoles)))));			
		case GLOBAL:
		default:
			return entityRepository.table(template, offset, limit, 
					generateSearchFields(searchField, searchValue, template));
		}
		
	}

	private SearchField[] generateSearchFields(String searchField, String searchValue, ObjectTemplate template, SearchField... additional) {
		List<SearchField> fields = new ArrayList<>();
		if(StringUtils.isNotNullOrEmpty(searchValue)) {
			FieldTemplate f = template.getField(searchField);
			switch(f.getFieldType()) {
			case OBJECT_REFERENCE:
				String resourceKey = f.getValidationValue(ValidationType.RESOURCE_KEY);
				ObjectTemplate t = templateService.get(resourceKey);
				Collection<String> uuids = new ArrayList<>();
				for(AbstractObject obj : entityRepository.list(t, SearchField.like(t.getNameField(), searchValue))) {
					uuids.add(obj.getUuid());
				}
				fields.add(SearchField.in(searchField, uuids));
				break;
			default:
				fields.add(SearchField.like(searchField, searchValue));
				break;
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
			return entityRepository.count(template,
					SearchField.eq("ownerUUID", getCurrentUser().getUuid()));
		case ASSIGNED:
			Collection<Role> userRoles = roleService.getRolesByUser(getCurrentUser());
			return entityRepository.count(template,
					SearchField.or(
							SearchField.in("users", getCurrentUser().getUuid()),
							SearchField.in("roles", UUIDObjectUtils.getUUIDs(userRoles))
					));			
		case GLOBAL:
		default:
			return entityRepository.count(template);
		}
		
	}
	
	@Override
	public long count(String resourceKey, String searchField, String searchValue) {
		
		ObjectTemplate template = templateService.get(resourceKey);
		switch(template.getScope()) {
		case PERSONAL:
			return entityRepository.count(template,
					generateSearchFields(searchField, searchValue, template, SearchField.eq("ownerUUID", getCurrentUser().getUuid())));
		case ASSIGNED:
			Collection<Role> userRoles = roleService.getRolesByUser(getCurrentUser());
			return entityRepository.count(template,
					generateSearchFields(searchField, searchValue, template, SearchField.or(
							SearchField.in("users", getCurrentUser().getUuid()),
							SearchField.in("roles", UUIDObjectUtils.getUUIDs(userRoles)))));			
		case GLOBAL:
		default:
			return entityRepository.count(template, generateSearchFields(searchField, searchValue, template));
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
			throw new IllegalStateException(StringUtils.format("%s is not a known form handler", handler));
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
