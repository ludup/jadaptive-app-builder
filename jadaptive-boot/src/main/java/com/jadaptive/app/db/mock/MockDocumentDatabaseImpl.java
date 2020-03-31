package com.jadaptive.app.db.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.UUIDEntity;
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
	public <E extends UUIDEntity> void insertOrUpdate(E obj, Document document, String table, String database) {
		
		if(StringUtils.isBlank(obj.getUuid())) {
			obj.setUuid(UUID.randomUUID().toString());
		}
		getCollection(table, database).put(obj.getUuid(), document);

	}

	@Override
	public Document get(String uuid, String table, String database) {
			
		Document doc = getCollection(table, database).get(uuid);
		
		if(Objects.isNull(doc)) {
			throw new EntityException(String.format("%s not found for id %s", table, uuid));
		}
		
		return doc;
	}

	@Override
	public void delete(String uuid, String table, String database) {
		
		get(uuid, table, database);
		getCollection(table, database).remove(uuid);
	}

	@Override
	public Iterable<Document> list(String table, String database, SearchField...fields) {	
		return getCollection(table, database).values();
	}
	
	@Override
	public Iterable<Document> table(String table, String searchField, String searchValue, String database, int start, int length) {
		Collection<Document> tmp = getCollection(table, database).values();
		return new ArrayList<>(tmp).subList(start, Math.min(start + length, tmp.size()-1));
	}

	@Override
	public Long count(String table, String database) {
		return new Long(getCollection(table, database).size());
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
	public Iterable<Document> searchTable(String table, String database, int start, int length, SearchField... fields) {
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
	public Document max(String table, String database, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document min(String table, String database, String field) {
		// TODO Auto-generated method stub
		return null;
	}

}
