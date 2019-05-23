package com.jadaptive.entity;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.entity.template.EntityTemplateImpl;
import com.jadaptive.entity.template.EntityTemplateRepository;
import com.jadaptive.repository.RepositoryException;

@Service
public class EntityTemplateServiceImpl implements EntityTemplateService {

	@Autowired
	EntityTemplateRepository repository; 
	
	@Override
	public EntityTemplateImpl get(String resourceKey) throws UnknownEntityException, RepositoryException, EntityNotFoundException {
		
		EntityTemplateImpl e = repository.get(resourceKey);
		
		if(Objects.isNull(e)) {
			throw new EntityNotFoundException(String.format("Cannot find entity with resource key %s", resourceKey));
		}
		
		return e;
	}

}