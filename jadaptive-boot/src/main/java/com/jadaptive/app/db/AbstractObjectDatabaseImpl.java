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
import com.jadaptive.utils.Utils;

public abstract class AbstractObjectDatabaseImpl implements AbstractObjectDatabase {

	protected final DocumentDatabase db;
	
	protected AbstractObjectDatabaseImpl(DocumentDatabase db) {
		this.db = db;
	}
	
	protected String getCollectionName(Class<?> clz) {
		return DocumentHelper.getTemplateResourceKey(clz);
	}
	
	protected <T extends AbstractUUIDEntity> void saveObject(T obj, String database) throws RepositoryException, EntityException {
		try {

			Document document = new Document();
			DocumentHelper.convertObjectToDocument(obj, document);
			
			db.insertOrUpdate(obj, document, getCollectionName(obj.getClass()), database);
			onObjectUpdated(obj);
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> T getObject(String uuid, String database, Class<T> clz) throws RepositoryException, EntityException {
		try {
			
			Document document = db.get(uuid, getCollectionName(clz), database);
			if(Objects.isNull(document)) {
				throw new EntityNotFoundException(String.format("Object from %s not found with id %s", 
						getCollectionName(clz), uuid));
			}
			return DocumentHelper.convertDocumentToObject(clz, document);
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> T getObject(String database, Class<T> clz, SearchField... fields) throws RepositoryException, EntityException {
		try {
			Document document = db.get(getCollectionName(clz), database, fields);
			if(Objects.isNull(document)) {
				throw new EntityNotFoundException(String.format("Object from %s not found for fields %s", 
						getCollectionName(clz),
						getSearchFieldsText(fields, "AND")));
			}
			return DocumentHelper.convertDocumentToObject(clz, document);
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> T max(String database, Class<T> clz, String field) throws RepositoryException, EntityException {
		try {
			Document document = db.max(getCollectionName(clz), database, field);
			if(Objects.isNull(document)) {
				throw new EntityNotFoundException(String.format("Maximum value from %s not found for fields %s", 
						getCollectionName(clz),
						field));
			}
			return DocumentHelper.convertDocumentToObject(clz, document);
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> T min(String database, Class<T> clz, String field) throws RepositoryException, EntityException {
		try {
			Document document = db.min(getCollectionName(clz), database, field);
			if(Objects.isNull(document)) {
				throw new EntityNotFoundException(String.format("Minimum value from %s not found for fields %s", 
						getCollectionName(clz),
						field));
			}
			return DocumentHelper.convertDocumentToObject(clz, document);
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	private Object getSearchFieldsText(SearchField[] fields, String condition) {
		
		StringBuffer buf = new StringBuffer();
		for(int i=0;i<fields.length;i++) {
			SearchField field = fields[i];
			switch(field.getSearchType()) {
			case AND:
				buf.append("(");
				buf.append(getSearchFieldsText(field.getFields(), " AND "));
				buf.append(")");
				break;
			case OR:
				buf.append("(");
				buf.append(getSearchFieldsText(field.getFields(), " OR "));
				buf.append(")");
				break;
			case EQUALS:
			case LIKE:
				buf.append(field.getColumn());
				buf.append("=");
				buf.append(field.getValue()[0]);
				break;
			case IN:
				buf.append(field.getColumn());
				buf.append(" IN(");
				buf.append(Utils.csv(field.getValue()));
				buf.append(")");
				break;
			case NOT:
				buf.append(field.getColumn());
				buf.append(" IS NOT ");
				buf.append(Utils.csv(field.getValue()));
				break;
			}
			
			if(i < fields.length-1) {
				buf.append(condition);
			}
		}
		
		return buf.toString();
	}

	private void checkException(Throwable e) throws EntityException {
		if(e instanceof EntityException) {
			throw (EntityException)e;
		}
	}
	
	protected <T extends AbstractUUIDEntity> void deleteObject(T obj, String database) throws RepositoryException, EntityException {
		try {
			if(obj.getSystem()) {
				throw new EntityException(String.format("You cannot delete system objects from %s", getCollectionName(obj.getClass())));
			}
			db.delete(obj.getUuid(), getCollectionName(obj.getClass()), database);
			onObjectDeleted(obj);
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> void onObjectDeleted(T obj) { }

	protected <T extends AbstractUUIDEntity> void onObjectCreated(T obj) { }
	
	protected <T extends AbstractUUIDEntity> void onObjectUpdated(T obj) { }
	
	protected <T extends AbstractUUIDEntity> Collection<T> listObjects(String database, Class<T> clz) throws RepositoryException, EntityException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.list(getCollectionName(clz), database)) {
				results.add(DocumentHelper.convertDocumentToObject(clz, document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> listObjects(String database, Class<T> clz, SearchField... fields) throws RepositoryException, EntityException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.list(getCollectionName(clz), database, fields)) {
				results.add(DocumentHelper.convertDocumentToObject(clz, document));
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
			for(Document document : db.search(getCollectionName(clz), database, fields)) {
				results.add(DocumentHelper.convertDocumentToObject(clz, document));
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
			for(Document document : db.searchTable(getCollectionName(clz), database, start, length, fields)) {
				results.add(DocumentHelper.convertDocumentToObject(clz, document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Long searchCount(String database, Class<T> clz, SearchField... fields) throws RepositoryException, EntityException {
		
		try {
			return db.searchCount(getCollectionName(clz), database, fields);			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	

	protected <T extends AbstractUUIDEntity> Collection<T> tableObjects(String database, Class<T> clz, String searchField, String searchValue, int start, int length) throws RepositoryException, EntityException {
		
		try {

			List<T> results = new ArrayList<>();
			for(Document document : db.table(getCollectionName(clz), searchField, searchValue, database, start, length)) {
				results.add(DocumentHelper.convertDocumentToObject(clz, document));
			}
			
			return results;
			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Long countObjects(String database, Class<T> clz) throws RepositoryException, EntityException {
		
		try {
			return db.count(getCollectionName(clz), database);			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
}
