package com.jadaptive.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.EntityTemplateService;
import com.jadaptive.permissions.PermissionService;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.repository.TransactionAdapter;
import com.jadaptive.templates.SystemTemplates;
import com.jadaptive.templates.TemplateEnabledService;

@Service
public class EntityServiceImpl implements EntityService, TemplateEnabledService<Entity> {

	@Autowired
	EntityRepository entityRepository;
	
	@Autowired
	EntityTemplateService templateService;
	
	@Autowired
	PermissionService permissionService; 
	
	@Override
	public Entity getSingleton(String resourceKey) throws RepositoryException, EntityException {

		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()!=EntityType.SINGLETON) {
			throw new EntityException(String.format("%s is not a singleton entity", resourceKey));
		}
		
		Entity e = entityRepository.get(resourceKey, resourceKey);
		if(!resourceKey.equals(e.getResourceKey())) {
			throw new IllegalStateException();
		}
		return e;
 	}
	
	@Override
	public Entity get(String resourceKey, String uuid) throws RepositoryException, EntityException {

		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()!=EntityType.COLLECTION) {
			throw new EntityException(String.format("%s is not a collection entity", resourceKey));
		}
		
		Entity e = entityRepository.get(uuid, resourceKey);
		if(!resourceKey.equals(e.getResourceKey())) {
			throw new IllegalStateException();
		}
		return e;
 	}


	@Override
	public Collection<Entity> list(String resourceKey) throws RepositoryException, EntityException {
		templateService.get(resourceKey);
		return entityRepository.list(resourceKey);
	}

	@Override
	public void saveOrUpdate(Entity entity) throws RepositoryException, EntityException {
		EntityTemplate template = templateService.get(entity.getResourceKey());
		if(template.getType()==EntityType.SINGLETON && !entity.getUuid().equals(entity.getResourceKey())) {	
			throw new EntityException("You cannot save a Singleton Entity with a new UUID");
		}
		entityRepository.save(entity);
		
	}

	@Override
	public void delete(String resourceKey, String uuid) throws RepositoryException, EntityException {
		
		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()==EntityType.SINGLETON) {	
			throw new EntityException("You cannot delete a Singleton Entity");
		}
		
		Entity e = get(resourceKey, uuid);
		if(e.getSystem()) {
			throw new EntityException("You cannot delete a system object");
		}
		entityRepository.delete(resourceKey, uuid);
	}

	@Override
	public void deleteAll(String resourceKey) throws EntityException {
		
		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()==EntityType.SINGLETON) {	
			throw new EntityException("You cannot delete a Singleton Entity");
		}
		
		entityRepository.deleteAll(resourceKey);
		
	}
	
	@Override
	public Integer getWeight() {
		return SystemTemplates.ENTITY.ordinal();
	}

	@Override
	public Class<Entity> getResourceClass() {
		return Entity.class;
	}

	@Override
	public String getName() {
		return "Entity";
	}

	@Override
	public Entity createEntity() {
		return new Entity();
	}

	@Override
	public String getResourceKey() {
		return "entity";
	}

	@Override
	public void saveTemplateObjects(List<Entity> objects, @SuppressWarnings("unchecked") TransactionAdapter<Entity>... ops) throws RepositoryException, EntityException {
		
		for(Entity obj : objects) {
			saveOrUpdate(obj);
			for(TransactionAdapter<Entity> op : ops) {
				op.afterSave(obj);
			}
		}
	}
		
	@Override
	public void onTemplatesComplete(String... resourceKeys) {
		
		for(String resourceKey : resourceKeys) {
			permissionService.registerStandardPermissions(resourceKey);
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
	public Collection<Entity> table(String resourceKey, String searchField, String searchValue, int offset, int limit) {
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
