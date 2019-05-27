package com.jadaptive.entity.template;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.RepositoryException;

@Service
public class EntityTemplateServiceImpl implements EntityTemplateService {

	@Autowired
	EntityTemplateRepository repository; 
	
	@Override
	public EntityTemplate get(String resourceKey) throws RepositoryException, EntityNotFoundException {
		
		EntityTemplate e = repository.get(resourceKey);
		
		if(Objects.isNull(e)) {
			throw new EntityNotFoundException(String.format("Cannot find entity with resource key %s", resourceKey));
		}
		
		return e;
	}

}