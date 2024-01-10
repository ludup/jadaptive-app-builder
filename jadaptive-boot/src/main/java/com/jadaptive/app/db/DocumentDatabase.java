package com.jadaptive.app.db;

import java.util.Set;

import org.bson.Document;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.template.SortOrder;

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

	Document find(String field, String value, String table, String database);

	Iterable<Document> list(String table, String database, SearchField... fields);

	Iterable<Document> search(String table, String database, SearchField... fields);
	
	Iterable<Document> search(String table, String database, SortOrder order, String sortField, SearchField... fields);

	Iterable<Document> searchTable(String table, String database, int start, int length, SortOrder order, String sortField, SearchField... fields);

	Long searchCount(String table, String database, SearchField... fields);

	void createTextIndex(String fieldName, String table, String database);

	void createUniqueIndex(String table, String database, String... fieldNames);

	void createIndex(String table, String database, String... fieldNames);

	Document max(String table, String database, String field, SearchField... fields);
	
	Document min(String table, String database, String field, SearchField... fields);

	void dropSchema();

	Iterable<Document> table(String table, String searchField, String searchValue, String database, int start, int length, SortOrder order, String sortField);

	void doInTransaction(Runnable r);

	Long sumLongValues(String table, String database, String groupBy, SearchField... fields);

	void dropIndexes(String table, String database);

	Set<String> getIndexNames(String table, String database);

	boolean isTransactionActive();

	Double sumDoubleValues(String table, String database, String groupBy, SearchField... fields);

}
