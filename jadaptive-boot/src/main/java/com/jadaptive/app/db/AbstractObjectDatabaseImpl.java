package com.jadaptive.app.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.bson.Document;

import com.jadaptive.api.db.AbstractObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;

public class AbstractObjectDatabaseImpl implements AbstractObjectDatabase {

	protected final DocumentDatabase db;
	
	protected AbstractObjectDatabaseImpl(DocumentDatabase db) {
		this.db = db;
	}
	
	protected <T extends AbstractUUIDEntity> void saveObject(T obj, String database) throws RepositoryException, EntityException {
		try {

			Document document = new Document();
			DocumentHelper.convertObjectToDocument(obj, document);
			
			db.insertOrUpdate(obj, document, obj.getClass().getSimpleName(), database);
			
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> T getObject(String uuid, String database, Class<T> clz) throws RepositoryException, EntityException {
		try {
			
			Document document = db.get(uuid, clz.getSimpleName(), database);
			if(Objects.isNull(document)) {
				throw new EntityNotFoundException(String.format("%s not found with id %s", clz.getSimpleName(), uuid));
			}
			return DocumentHelper.convertDocumentToObject(clz.newInstance(), document);
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> T getObject(String field, String value, String database, Class<T> clz) throws RepositoryException, EntityException {
		try {
			Document document = db.find(field, value, clz.getSimpleName(), database);
			if(Objects.isNull(document)) {
				throw new EntityNotFoundException(String.format("%s not found with %s %s", clz.getSimpleName(), field, value));
			}
			return DocumentHelper.convertDocumentToObject(clz.newInstance(), document);
			
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
			if(obj.getSystem()) {
				throw new EntityException(String.format("You cannot delete a system %s", obj.getClass().getSimpleName()));
			}
			db.delete(obj.getUuid(), obj.getClass().getSimpleName(), database);
			
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> listObjects(String database, Class<T> clz) throws RepositoryException, EntityException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.list(clz.getSimpleName(), database)) {
				results.add(DocumentHelper.convertDocumentToObject(clz.newInstance(), document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> listObjects(String field, String value, String database, Class<T> clz) throws RepositoryException, EntityException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.list(field, value, clz.getSimpleName(), database)) {
				results.add(DocumentHelper.convertDocumentToObject(clz.newInstance(), document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> searchObjects(String database, Class<T> clz, SearchField... fields) throws RepositoryException, EntityException {
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.search(clz.getSimpleName(), database, fields)) {
				results.add(DocumentHelper.convertDocumentToObject(clz.newInstance(), document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> searchTable(String database, Class<T> clz, int start, int length, SearchField... fields) throws RepositoryException, EntityException {
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.searchTable(clz.getSimpleName(), database, start, length, fields)) {
				results.add(DocumentHelper.convertDocumentToObject(clz.newInstance(), document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Long searchCount(String database, Class<T> clz, SearchField... fields) throws RepositoryException, EntityException {
		
		try {
			return db.searchCount(clz.getSimpleName(), database, fields);			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	

	protected <T extends AbstractUUIDEntity> Collection<T> tableObjects(String database, Class<T> clz, String searchField, String searchValue, int start, int length) throws RepositoryException, EntityException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.table(clz.getSimpleName(), searchField, searchValue, database, start, length)) {
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
			return db.count(clz.getSimpleName(), database);			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
}
