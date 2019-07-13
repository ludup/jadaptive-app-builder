package com.jadaptive.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

public abstract class AbstractObjectDatabaseImpl implements AbstractObjectDatabase {

	@Autowired
	protected MongoDatabaseService mongo;
	
	protected <T extends AbstractUUIDEntity> void saveObject(T obj, String database) throws RepositoryException, EntityException {
		try {
			MongoDatabase db = mongo.getClient().getDatabase(database);
			MongoCollection<Document> collection = db.getCollection(obj.getClass().getName());

			Document document = new Document();
			DocumentHelper.convertObjectToDocument(obj, document);
			
			if(StringUtils.isBlank(obj.getUuid())) {
				collection.insertOne(document);
			} else {
				collection.replaceOne(Filters.eq("_id", obj.getUuid()), 
						document, new ReplaceOptions().upsert(true));
			}
			
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> T getObject(String uuid, String database, Class<T> clz) throws RepositoryException, EntityException {
		try {
			
			MongoDatabase db = mongo.getClient().getDatabase(database);
			MongoCollection<Document> collection = db.getCollection(clz.getName());
			
			Document document = collection.find(Filters.eq("_id", uuid)).first();
			if(Objects.isNull(document)) {
				throw new EntityException(String.format("Object not found with id %s", uuid));
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
			MongoDatabase db = mongo.getClient().getDatabase(database);
			MongoCollection<Document> collection = db.getCollection(obj.getClass().getName());

			Document document = collection.find(Filters.eq("_id", obj.getUuid())).first();
			
			if(Objects.isNull(document)) {
				throw new EntityException(String.format(
						"%s does not exist with an id of %s", 
						obj.getClass().getSimpleName(),
						obj.getUuid()));
			}
			
			collection.deleteOne(Filters.eq("_id", obj.getUuid()));
			
		} catch(Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected <T extends AbstractUUIDEntity> Collection<T> listObjects(String database, Class<T> clz) throws RepositoryException, EntityException {
		
		try {
			
			MongoDatabase db = mongo.getClient().getDatabase(database);
			MongoCollection<Document> collection = db.getCollection(clz.getName());

			List<T> results = new ArrayList<>();
			
			for(Document document : collection.find()) {
				results.add(DocumentHelper.convertDocumentToObject(clz.newInstance(), document));
			}
			
			return results;

			
		} catch (Throwable e) {
			checkException(e);
			throw new RepositoryException(e.getMessage(), e);
		}
	}
}
