package com.jadaptive.app.db.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.app.db.DocumentDatabase;

public class MockDocumentDatabaseImpl implements DocumentDatabase {

	Map<String,Map<String,Map<String,Document>>> documents = new ConcurrentHashMap<>();

	private Map<String,Document> getCollection(String table, String database) {
		if(!documents.containsKey(database)) {
			documents.put(database, new HashMap<String,Map<String,Document>>());
		}
		if(!documents.get(database).containsKey(table)) {
			documents.get(database).put(table, new HashMap<String,Document>());
		}
		return documents.get(database).get(table);
	}
	
	@Override
	public void insertOrUpdate(Document document, String table, String database) {
		
		if(StringUtils.isBlank(document.getString("_id"))) {
			document.put("_id", UUID.randomUUID().toString());
		}
		getCollection(table, database).put(document.getString("_id"), document);

	}

	@Override
	public Document getByUUID(String uuid, String table, String database) {
			
		Document doc = getCollection(table, database).get(uuid);
		
		if(Objects.isNull(doc)) {
			throw new ObjectException(String.format("%s not found for id %s", table, uuid));
		}
		
		return doc;
	}

	@Override
	public void deleteByUUID(String uuid, String table, String database) {
		
		getByUUID(uuid, table, database);
		getCollection(table, database).remove(uuid);
	}

	@Override
	public Iterable<Document> list(String table, String database, SearchField...fields) {	
		return getCollection(table, database).values();
	}
	
	@Override
	public Iterable<Document> table(String table, String searchField, String searchValue, String database, int start, int length, SortOrder order, String sortField) {
		Collection<Document> tmp = getCollection(table, database).values();
		return new ArrayList<>(tmp).subList(start, Math.min(start + length, tmp.size()-1));
	}

	@Override
	public Long count(String table, String database, SearchField... fields) {
		return Long.valueOf(getCollection(table, database).size());
	}

	@Override
	public void dropCollection(String table, String database) {
		if(documents.containsKey(database)) {
			documents.get(database).remove(table);
		}
	}

	@Override
	public Document getFirst(String uuid, String table, String database) {
		return getCollection(table, database).get(uuid);
	}

	@Override
	public void dropDatabase(String database) {
		documents.remove(database);
	}

	@Override
	public Document find(String field, String value, String table, String database) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public Iterable<Document> searchCollectionField(String field, String value, String table, String database) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Long count(String table, String searchField, String searchValue, String database) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Document> search(String table, String database, SearchField... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Document> searchTable(String table, String database, int start, int length, SortOrder order, String sortField, SearchField... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long searchCount(String table, String database, SearchField... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document get(String table, String database, SearchField... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createTextIndex(String fieldName, String table, String database) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createUniqueIndex(String table, String database, String... fieldNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createIndex(String table, String database, String... fieldNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Document max(String table, String database, String field, SearchField... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document min(String table, String database, String field, SearchField... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String table, String database, SearchField... fields) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dropSchema() {

	}

	@Override
	public Iterable<Document> search(String table, String database, SortOrder order, String sortField,
			SearchField... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doInTransaction(Runnable r) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long sumLongValues(String table, String database, String groupBy, SearchField... fields) {
		// TODO Auto-generated method stub
		return 0L;
	}

	@Override
	public void dropIndexes(String table, String database) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getIndexNames(String table, String database) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTransactionActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Double sumDoubleValues(String table, String database, String groupBy, SearchField... fields) {
		// TODO Auto-generated method stub
		return null;
	}

}
