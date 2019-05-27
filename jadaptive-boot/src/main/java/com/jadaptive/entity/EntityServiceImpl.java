package com.jadaptive.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.repository.RepositoryException;

@Service
public class EntityServiceImpl implements EntityService {

	@Autowired
	EntityRepository entityRepository;
	

	@Override
	public Entity get(String resourceKey, String uuid) throws RepositoryException, EntityNotFoundException {

		Entity e = entityRepository.get(uuid);
		if(!resourceKey.equals(e.getResourceKey())) {
			throw new IllegalStateException();
		}
		return e;
 	}

}
