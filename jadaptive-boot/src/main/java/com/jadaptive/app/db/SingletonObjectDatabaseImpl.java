package com.jadaptive.app.db;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.SingletonUUIDEntity;

@Repository
public class SingletonObjectDatabaseImpl<T extends SingletonUUIDEntity> implements SingletonObjectDatabase<T> {

	@Autowired
	private TenantAwareObjectDatabase<T> objectDatabase;
	
	@Override
	public T getObject(Class<T> resourceClass) {
		
		try {
			T tmp = resourceClass.getConstructor().newInstance();
			try {
				return objectDatabase.get(tmp.getResourceKey(), resourceClass);
			} catch(ObjectNotFoundException e) {
				Document doc = new Document();
				DocumentHelper.buildDocument(tmp.getResourceKey(), tmp, doc);
				return DocumentHelper.convertDocumentToObject(resourceClass, doc);
			}	
		} catch(Throwable t) {
			throw new RepositoryException(t.getMessage(), t);
		}
	}

	@Override
	public void saveObject(T obj) {
		objectDatabase.saveOrUpdate(obj);
	}

}
