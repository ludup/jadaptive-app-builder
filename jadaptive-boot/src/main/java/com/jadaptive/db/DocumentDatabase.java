package com.jadaptive.db;

import org.bson.Document;

import com.jadaptive.repository.AbstractUUIDEntity;

public interface DocumentDatabase {

	<E extends AbstractUUIDEntity> void insertOrUpdate(E obj, Document document, String table, String database);

	Document get(String uuid, String table, String database);

	void delete(String uuid, String table, String database);

	Iterable<Document> list(String table, String database);

	Long count(String name, String database);

	void dropCollection(String resourceKey, String uuid);

	Document getFirst(String uuid, String resourceKey, String uuid2);

	void dropDatabase(String uuid);

}
