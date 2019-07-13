package com.jadaptive.entity.template;

import java.util.Collection;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.entity.EntityException;
import com.jadaptive.entity.EntityService;
import com.jadaptive.repository.RepositoryException;

@Service
public class EntityTemplateServiceImpl implements EntityTemplateService {

	@Autowired
	EntityTemplateRepository repository; 
	
	
	@Autowired
	EntityService entityService;
	
	@Override
	public EntityTemplate get(String resourceKey) throws RepositoryException, EntityException {
		
		EntityTemplate e = repository.get(resourceKey);
		
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Cannot find entity with resource key %s", resourceKey));
		}
		
		return e;
	}

	@Override
	public Collection<EntityTemplate> list() throws RepositoryException, EntityException {
		return repository.list();
	}

	@Override
	public void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException {
		
		repository.saveOrUpdate(template);
		
	}

	@Override
	public void delete(String uuid) throws EntityException {
		
		entityService.deleteAll(uuid);
		repository.delete(uuid);
		
	}

}