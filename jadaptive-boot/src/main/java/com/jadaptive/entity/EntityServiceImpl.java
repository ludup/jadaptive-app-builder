package com.jadaptive.entity;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.EntityTemplateService;
import com.jadaptive.repository.RepositoryException;

@Service
public class EntityServiceImpl implements EntityService {

	@Autowired
	EntityRepository entityRepository;
	
	@Autowired
	EntityTemplateService templateService;
	
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
}
