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
	public Entity getSingleton(String resourceKey) throws RepositoryException, EntityNotFoundException {

		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()!=EntityType.SINGLETON) {
			throw new EntityNotFoundException(String.format("%s is not a singleton entity", resourceKey));
		}
		
		Entity e = entityRepository.get(resourceKey, resourceKey);
		if(!resourceKey.equals(e.getResourceKey())) {
			throw new IllegalStateException();
		}
		return e;
 	}
	
	@Override
	public Entity get(String resourceKey, String uuid) throws RepositoryException, EntityNotFoundException {

		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()!=EntityType.COLLECTION) {
			throw new EntityNotFoundException(String.format("%s is not a collection entity", resourceKey));
		}
		
		Entity e = entityRepository.get(uuid, resourceKey);
		if(!resourceKey.equals(e.getResourceKey())) {
			throw new IllegalStateException();
		}
		return e;
 	}


	@Override
	public Collection<Entity> list(String resourceKey) throws RepositoryException, EntityNotFoundException {
		EntityTemplate template = templateService.get(resourceKey);
		return entityRepository.list(resourceKey);
	}

	@Override
	public void saveOrUpdate(String resourceKey, Entity entity) throws RepositoryException, EntityNotFoundException {
		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()==EntityType.SINGLETON && !entity.getUuid().equals(resourceKey)) {	
			throw new EntityNotFoundException("You cannot save a Singleton Entity with a new UUID");
		}
		entityRepository.save(entity, resourceKey);
		
	}

	@Override
	public void delete(String resourceKey, String uuid) throws RepositoryException, EntityNotFoundException {
		
		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()==EntityType.SINGLETON) {	
			throw new EntityNotFoundException("You cannot delete a Singleton Entity");
		}
		
		Entity e = get(resourceKey, uuid);
		if(e.getSystem()) {
			throw new EntityNotFoundException("You cannot delete a system object");
		}
		entityRepository.delete(resourceKey, uuid);
	}

	@Override
	public void deleteAll(String resourceKey) throws EntityNotFoundException {
		
		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()==EntityType.SINGLETON) {	
			throw new EntityNotFoundException("You cannot delete a Singleton Entity");
		}
		
		entityRepository.deleteAll(resourceKey);
		
	}
}
