package com.jadaptive.app.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SearchField.Type;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
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
		return collection.find(Filters.eq("_id", uuid)).first();

	}
	


	@Override
	public Document get(String table, String database, SearchField... fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		return collection.find(buildFilter(fields)).first();

	}
	
	@Override
	public Document find(String field, String value, String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		return collection.find(Filters.eq(field, value)).first();

	}

	@Override
	public void delete(String uuid, String table, String database) {
		
		get(uuid, table, database);
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		collection.deleteOne(Filters.eq("_id", uuid));
		
	}
	
	@Override
	public Iterable<Document> list(String table, String database, SearchField... fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		if(fields.length == 0) {
			return collection.find();
		} else {
			return collection.find(buildFilter(fields));
		}
	}
	
	@Override
	public Iterable<Document> search(String table, String database, SearchField...fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		if(fields.length == 0) {
			return collection.find();
		} else {
			return collection.find(buildFilter(fields));
		}
	}
	
	@Override
	public Iterable<Document> searchTable(String table, String database, int start, int length, SearchField...fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		if(fields.length == 0) {
			return collection.find().skip(start).limit(length);
		} else {
			return collection.find(buildFilter(fields)).skip(start).limit(length);
		}
	}
		
	@Override
	public Long searchCount(String table, String database, SearchField... fields) {
		MongoCollection<Document> collection = getCollection(table, database);
		if(fields.length == 0) {
			return collection.countDocuments();
		} else {
			return collection.countDocuments(buildFilter(fields));
		}
	}
	
	@Override
	public Iterable<Document> table(String table, String searchField, String searchValue, String database, int start, int length) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		searchField = configureSearch(searchField);
		if(StringUtils.isBlank(searchValue)) {
			return collection.find().skip(start).limit(length);
		} else {
			return collection.find(Filters.regex(searchField, searchValue)).skip(start).limit(length);
		}
	}

	@Override
	public Long count(String table, String database) {
		MongoCollection<Document> collection = getCollection(table, database);
		return collection.countDocuments();
	}
	
	@Override
	public Long count(String table, String searchField, String searchValue, String database) {
		MongoCollection<Document> collection = getCollection(table, database);
		searchField = configureSearch(searchField);
		if(StringUtils.isBlank(searchValue)) {
			return collection.countDocuments();
		} else {
			return collection.countDocuments(Filters.regex(searchField, searchValue));
		}
		
	}
	
	protected String configureSearch(String searchField) {
		if(StringUtils.isBlank(searchField) || searchField.equalsIgnoreCase("UUID")) {
			searchField = "_id";
		}
		return searchField;
	}

	@Override
	public void dropCollection(String table, String database) {
		getCollection(table, database).drop();
	}

	@Override
	public Document getFirst(String uuid, String table, String database) {
		Document e = getCollection(table, database).find(Filters.eq("_id", uuid)).first();
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("id %s for entity %s was not found", uuid, table));
		}
		return e;
	}

	@Override
	public void dropDatabase(String database) {
		mongo.getClient().getDatabase(database).drop();
	}
	
	public Bson buildFilter(SearchField...fields) {
		return buildFilter(Type.AND, fields);
	}
	
	public Bson buildFilter(SearchField.Type queryType, SearchField...fields) {
		
		List<Bson> tmp = new ArrayList<>();
		for(SearchField field : fields) {
			switch(field.getSearchType()) {
			case EQUALS:
				tmp.add(Filters.eq(field.getColumn(), field.getValue()[0]));
				break;
			case IN:
				tmp.add(Filters.in(field.getColumn(), field.getValue()));
				break;
			case LIKE:
				tmp.add(Filters.regex(field.getColumn(), field.getValue()[0]));
				break;
			case OR:
				tmp.add(buildFilter(SearchField.Type.OR, field.getFields()));
				break;
			case AND:
				tmp.add(buildFilter(SearchField.Type.AND, field.getFields()));
				break;
			}
		}

		if(tmp.size() > 1) {
			switch(queryType) {
			case AND:
				return Filters.and(tmp);
			case OR:
				return Filters.or(tmp);
			default:
				throw new IllegalArgumentException("Invalid use of SearchField.Type");
			}
		}
		
		return tmp.get(0);
	}


}
