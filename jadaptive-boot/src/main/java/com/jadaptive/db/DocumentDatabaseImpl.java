package com.jadaptive.db;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

@Repository
public class DocumentDatabaseImpl implements DocumentDatabase {

	@Autowired
	protected MongoDatabaseService mongo;
	
	private MongoCollection<Document> getCollection(String table, String database) {
		MongoDatabase db = mongo.getClient().getDatabase(database);
		return db.getCollection(table);
	}
	
	@Override
	public <E extends AbstractUUIDEntity> void insertOrUpdate(E obj, Document document, String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		if(StringUtils.isBlank(obj.getUuid())) {
			obj.setUuid(UUID.randomUUID().toString());
			document.put("_id", obj.getUuid());
			collection.insertOne(document);			
		} else {
			collection.replaceOne(Filters.eq("_id", obj.getUuid()), 
					document, new ReplaceOptions().upsert(true));
		}
	}

	@Override
	public Document get(String uuid, String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		Document document = collection.find(Filters.eq("_id", uuid)).first();
		if(Objects.isNull(document)) {
			throw new EntityException(String.format("%s not found with id %s", table, uuid));
		}
		
		return document;
	}

	@Override
	public void delete(String uuid, String table, String database) {
		
		get(uuid, table, database);
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		collection.deleteOne(Filters.eq("_id", uuid));
		
	}
	
	@Override
	public Iterable<Document> list(String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		return collection.find();
	}
	
	@Override
	public Iterable<Document> table(String table, String database, int start, int length) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		return collection.find().skip(start).limit(length);
	}

	@Override
	public Long count(String table, String database) {
		MongoCollection<Document> collection = getCollection(table, database);
		return collection.countDocuments();
	}

	@Override
	public void dropCollection(String table, String database) {
		getCollection(table, database).drop();
	}

	@Override
	public Document getFirst(String uuid, String table, String database) {
		Document e = getCollection(table, database).find(Filters.eq("_id", uuid)).first();
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Uuid %s for entity %s was not found", uuid, table));
		}
		return e;
	}

	@Override
	public void dropDatabase(String database) {
		mongo.getClient().dropDatabase(database);
		
	}
}
