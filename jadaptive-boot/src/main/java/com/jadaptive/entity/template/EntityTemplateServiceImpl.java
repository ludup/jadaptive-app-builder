package com.jadaptive.entity.template;

import java.util.Collection;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.entity.EntityService;
import com.jadaptive.repository.RepositoryException;

@Service
public class EntityTemplateServiceImpl implements EntityTemplateService {

	@Autowired
	EntityTemplateRepository repository; 
	
	
	@Autowired
	EntityService entityService;
	
	@Override
	public EntityTemplate get(String resourceKey) throws RepositoryException, EntityNotFoundException {
		
		EntityTemplate e = repository.get(resourceKey);
		
		if(Objects.isNull(e)) {
			throw new EntityNotFoundException(String.format("Cannot find entity with resource key %s", resourceKey));
		}
		
		return e;
	}

	@Override
	public Collection<EntityTemplate> list() {
		return repository.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveOrUpdate(EntityTemplate template) {
		
		repository.save(template);
		
	}

	@Override
	public void delete(String uuid) throws EntityNotFoundException {
		
		entityService.deleteAll(uuid);
		repository.delete(uuid);
		
	}

}