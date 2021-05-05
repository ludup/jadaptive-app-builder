package com.jadaptive.app.db;

import org.bson.Document;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.ObjectTemplate;

public interface DocumentDatabase {

	void insertOrUpdate(Document document, String table, String database);

	Document getByUUID(String uuid, String table, String database);
	
	Document get(String table, String database, SearchField... fields);

	void deleteByUUID(String uuid, String table, String database);
	
	void delete(String table, String database, SearchField... fields);

	Long count(String table, String database, SearchField... fields);

	Long count(String table, String searchField, String searchValue, String database);
	
	void dropCollection(String table, String uuid);

	Document getFirst(String uuid, String resourceKey, String uuid2);

	void dropDatabase(String uuid);

	Iterable<Document> table(String table, String field, String value, String database, int start, int length);

	Document find(String field, String value, String table, String database);

	Iterable<Document> list(String table, String database, SearchField... fields);

	Iterable<Document> search(String table, String database, SearchField... fields);

	Iterable<Document> searchTable(String table, String database, int start, int length, SearchField... fields);

	Long searchCount(String table, String database, SearchField... fields);

	void createTextIndex(String fieldName, String table, String database);

	void createUniqueIndex(String table, String database, String... fieldNames);

	void createIndex(String table, String database, String... fieldNames);

	Document max(String table, String database, String field);
	
	Document min(String table, String database, String field);

	

	
	

}
