package com.jadaptive.app.db;

import org.bson.Document;

import com.jadaptive.app.repository.AbstractUUIDEntity;

public interface DocumentDatabase {

	<E extends AbstractUUIDEntity> void insertOrUpdate(E obj, Document document, String table, String database);

	Document get(String uuid, String table, String database);

	void delete(String uuid, String table, String database);

	Iterable<Document> list(String table, String database);

	Long count(String name, String database);

	Long count(String table, String searchField, String searchValue, String database);
	
	void dropCollection(String resourceKey, String uuid);

	Document getFirst(String uuid, String resourceKey, String uuid2);

	void dropDatabase(String uuid);

	Iterable<Document> table(String table, String field, String value, String database, int start, int length);

	Document find(String field, String value, String table, String database);

	Iterable<Document> list(String field, String value, String table, String database);

	Iterable<Document> matchCollectionField(String field, String value, String table, String database);

	

}
