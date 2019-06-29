package com.jadaptive.entity;

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
	public Entity get(String resourceKey, String uuid) throws RepositoryException, EntityNotFoundException {

		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()!=EntityType.COLLECTION) {
			throw new EntityNotFoundException(String.format("%s is not a collection entity", resourceKey));
		}
		
		Entity e = entityRepository.get(uuid);
		if(!resourceKey.equals(e.getResourceKey())) {
			throw new IllegalStateException();
		}
		return e;
 	}
	
	@Override
	public Entity get(String resourceKey) throws RepositoryException, EntityNotFoundException {

		EntityTemplate template = templateService.get(resourceKey);
		if(template.getType()!=EntityType.SINGLETON) {
			throw new EntityNotFoundException(String.format("%s is not a singleton entity", resourceKey));
		}
		
		Entity e = entityRepository.get(resourceKey);
		if(!resourceKey.equals(e.getResourceKey())) {
			throw new IllegalStateException();
		}
		return e;
 	}

}
