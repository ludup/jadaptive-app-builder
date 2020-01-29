package com.jadaptive.app.tenant;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.user.User;

@Repository
public class PersonalObjectDatabaseImpl<T extends PersonalUUIDEntity> 
		 implements PersonalObjectDatabase<T> {

	@Autowired
	TenantAwareObjectDatabase<T> objectDatabase;
	
	@Override
	public Collection<T> getPersonalObjects(Class<T> resourceClass, User user) {
		
		return objectDatabase.searchObjects(resourceClass, 
				SearchField.eq("ownerUUID", user.getUuid()));
	}
	
	@Override
	public void saveOrUpdate(T obj, User user) {
		obj.setOwnerUUID(user.getUuid());
		objectDatabase.saveOrUpdate(obj);
	}
}
