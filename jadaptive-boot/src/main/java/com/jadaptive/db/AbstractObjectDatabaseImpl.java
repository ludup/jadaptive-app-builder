package com.jadaptive.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bson.Document;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;

public abstract class AbstractObjectDatabaseImpl implements AbstractObjectDatabase {

	protected final DocumentDatabase db;
	
	protected AbstractObjectDatabaseImpl(DocumentDatabase db) {
		this.db = db;
	}
	
	protected <T extends AbstractUUIDEntity> void saveObject(T obj, String database) throws RepositoryException, EntityException {
		try {

			Document document = new Document();
			DocumentHelper.convertObjectToDocument(obj, document);
			
			db.insertOrUpdate(obj, document, obj.getClass().getName(), database);
			
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> T getObject(String uuid, String database, Class<T> clz) throws RepositoryException, EntityException {
		try {
			
			return DocumentHelper.convertDocumentToObject(clz.newInstance(), db.get(uuid, clz.getName(), database));
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	private void checkException(Throwable e) throws EntityException {
		if(e instanceof EntityException) {
			throw (EntityException)e;
		}
	}
	
	protected <T extends AbstractUUIDEntity> void deleteObject(T obj, String database) throws RepositoryException, EntityException {
		try {
			
			db.delete(obj.getUuid(), obj.getClass().getName(), database);
			
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> listObjects(String database, Class<T> clz) throws RepositoryException, EntityException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.list(clz.getName(), database)) {
				results.add(DocumentHelper.convertDocumentToObject(clz.newInstance(), document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Long countObjects(String database, Class<T> clz) throws RepositoryException, EntityException {
		
		try {
			return db.count(clz.getName(), database);			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
}
