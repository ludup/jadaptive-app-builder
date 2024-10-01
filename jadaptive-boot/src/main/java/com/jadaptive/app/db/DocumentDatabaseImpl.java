package com.jadaptive.app.db;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.csv.CsvImportService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SearchField.Type;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.utils.Utils;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;

@Repository
public class DocumentDatabaseImpl implements DocumentDatabase {

	static Logger log = LoggerFactory.getLogger(DocumentDatabaseImpl.class);
	
	@Autowired
	protected MongoDatabaseService mongo;
	
	@Autowired
	private CsvImportService importService; 
	
	ThreadLocal<ClientSession> currentSession = new ThreadLocal<>();
	
	private MongoCollection<Document> getCollection(String table, String database) {
		MongoDatabase db = mongo.getClient().getDatabase(getDatabaseName(database));
		return db.getCollection(table);
	}
	
	private String getDatabaseName(String database) {
		String prefix = ApplicationProperties.getValue("mongodb.databasePrefix", null);
		if(Objects.nonNull(prefix)) {
			return String.format("%s-%s", prefix, database);
		}
		return database;
	}

	@Override
	public void dropSchema() {

		String prefix = ApplicationProperties.getValue("mongodb.databasePrefix", null);
		
		for(String database : mongo.getClient().listDatabaseNames()) {
			MongoDatabase db = mongo.getClient().getDatabase(getDatabaseName(database));
			switch(db.getName()) {
			case "admin":
			case "local":
			case "config":
				continue;
			default:
				if(db.getName().startsWith(prefix)) {
					db.drop();
				}
			}
		}
	}
	
	@Override
	public void doInTransaction(Runnable r) {
		
		if(isTransactionActive()) {
			r.run();
		} else {
			currentSession.set(mongo.getClient().startSession());
			if(log.isDebugEnabled()) {
				log.debug("Starting transaction");
			}
			currentSession.get().startTransaction();
			try {
				r.run();
				if(log.isDebugEnabled()) {
					log.debug("Committing transaction");
				}
				currentSession.get().commitTransaction();
			} catch(Throwable t) {
				if(log.isDebugEnabled()) {
					log.debug("Aborting transaction");
				}
				currentSession.get().abortTransaction();
				if(t instanceof RuntimeException) {
					throw t;
				}
				throw new IllegalStateException("Transaction failed with " + t.getMessage(), t);
			} finally {
				if(log.isDebugEnabled()) {
					log.debug("Closing session");
				}
				currentSession.get().close();
				currentSession.remove();
			}
		}
	}
	
	@Override
	public void createTextIndex(String fieldName, String table, String database) {
		String indexName = "text_" + fieldName;
		MongoCollection<Document> collection = getCollection(table, database);
		IndexOptions indexOptions = new IndexOptions()
				.collation(getCollation())
				.name(indexName);
		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			collection.createIndex(session, Indexes.text(fieldName), indexOptions);
		} else {
			collection.createIndex(Indexes.text(fieldName), indexOptions);		
		}
	}
	
	private Collation getCollation() {
		return Collation.builder().collationStrength(CollationStrength.PRIMARY)
				.locale(Locale.getDefault().getLanguage())
				.build();
	}

	@Override
	public void createIndex(String table, String database, String... fieldNames) {
		String indexName = "index_" + StringUtils.join(fieldNames, "_");
		MongoCollection<Document> collection = getCollection(table, database);
		IndexOptions indexOptions = new IndexOptions()
				.collation(getCollation())
				.name(indexName);
		
		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			collection.createIndex(session, Indexes.ascending(fieldNames), indexOptions);
		} else {
			collection.createIndex(Indexes.ascending(fieldNames), indexOptions);		
		}
	}
	
	@Override
	public void dropIndexes(String table, String database) {
		MongoCollection<Document> collection = getCollection(table, database);

		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			collection.dropIndexes(session);
		} else {
			collection.dropIndexes();	
		}
	}
	
	@Override
	public void createUniqueIndex(String table, String database, String...fieldNames) {
		String indexName = "unique_" + StringUtils.join(fieldNames, "_");
		MongoCollection<Document> collection = getCollection(table, database);
		IndexOptions indexOptions = new IndexOptions()
				.collation(Collation.builder().collationStrength(CollationStrength.PRIMARY)
						.locale(Locale.getDefault().getLanguage())
						.caseLevel(false).build())
				.unique(true)
				.name(indexName);
		
		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			collection.createIndex(session, Indexes.ascending(fieldNames), indexOptions);
		} else {
			collection.createIndex(Indexes.ascending(fieldNames), indexOptions);
		}
	}
	
	@Override
	public void insertOrUpdate(Document document, /*ObjectTemplate template,*/ String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);

		Date now = new Date();
		
		if(!importService.isImporting()) {
			document.put("lastModified", now);
		}

		assertUniqueConstraints(collection, document, document.getString("_id"));
		
		if(StringUtils.isBlank(document.getString("_id"))) {
			
			if(!document.containsKey("created")) {
				document.put("created", now);
			}
			document.put("_id", UUID.randomUUID().toString());
			
//			String contentHash = DocumentHelper.generateContentHash(document);
//			document.put("contentHash", contentHash);
			ClientSession session = currentSession.get();
			if(Objects.nonNull(session)) {
				collection.insertOne(session, document);	
			} else {
				collection.insertOne(document);		
			}
			
			// Created Event
		} else {
			
//			Document previous = null;
//			try {
//				previous = getByUUID(document.getString("_id"), table, database);
//			} catch(ObjectNotFoundException ex) {
//			}
			
//			String contentHash = DocumentHelper.generateContentHash(document);
//			document.put("contentHash", contentHash);
			
//			if(Objects.nonNull(previous) && previous.getString("contentHash").equals(contentHash)) {
//				if(log.isDebugEnabled()) {
//					log.debug("Object on {} with uuid {} has not been updated because it's new content hash is the same as the previous",
//							table, previous.get("_id"));
//				}
//				return;
//			}
			
//			if(log.isDebugEnabled()) {
//				log.debug("Saving {} object with content hash {}", table, contentHash);
//			}
			ClientSession session = currentSession.get();
			if(Objects.nonNull(session)) {
				collection.replaceOne(session, Filters.eq("_id", document.getString("_id")), 
						document, new ReplaceOptions().upsert(true));
			} else {
				collection.replaceOne(Filters.eq("_id", document.getString("_id")), 
						document, new ReplaceOptions().upsert(true));
			}
			
			
			
			// Updated Event
		}
	}
	
	private void assertUniqueConstraints(MongoCollection<Document> collection, Document document, String existingUUID) {
		
		Iterable<Document> indexes;
		
		indexes = collection.listIndexes();
			
		for(Document index : indexes) {
			if(index.getBoolean("unique", false)) {
				Document key = (Document) index.get("key");
				if(key.size()==1) {
					String field = key.keySet().iterator().next();
					if(collection.countDocuments(
							Filters.and(Filters.eq(field, document.get(field)),
									Filters.not(Filters.eq("_id", existingUUID)))) > 0L) {
						throw new RepositoryException(String.format("An object already exists with the same %s value '%s'!", 
								WordUtils.capitalize(field), document.get(field)));
					}
				}
			}
		}
	}

	@Override
	public Document getByUUID(String uuid, String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		FindIterable<Document> result;
		
		ClientSession session= currentSession.get();
		if(Objects.nonNull(session)) {
			result = collection.find(session, Filters.eq("_id", uuid));
		} else {
			result = collection.find(Filters.eq("_id", uuid));
		}

		if(!result.cursor().hasNext()) {
			throw new ObjectNotFoundException(String.format("Collection %s does not contain an object with id %s", table, uuid));
		}
		return result.first();
	}

	@Override
	public Document get(String table, String database, SearchField... fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		FindIterable<Document> result;
		
		ClientSession session= currentSession.get();
		if(Objects.nonNull(session)) {
			result = collection.find(session, buildFilter(fields))
					.collation(getCollation());
		} else {
			result = collection.find(buildFilter(fields))
					.collation(getCollation());
		}

		if(!result.cursor().hasNext()) {
			throw new ObjectNotFoundException(String.format("Collection %s does not contain an object for search %s", table, buildSearchString(fields)));
		}
		return result.first();
	}

	private String buildSearchString(SearchField[] fields) {
		StringBuilder builder = new StringBuilder();
		buildSearchString(Type.AND, builder, fields);
		return builder.toString();
	}

	@Override
	public Document max(String table, String database, String field, SearchField... fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		FindIterable<Document> result;
		
		ClientSession session= currentSession.get();
		if(fields.length > 0) {
			if(Objects.nonNull(session)) {
				result = collection.find(session, buildFilter(fields)).sort(new BasicDBObject(field, -1));
			} else {
				result = collection.find(buildFilter(fields)).sort(new BasicDBObject(field, -1));
			}
		} else {
			if(Objects.nonNull(session)) {
				result = collection.find(session).sort(new BasicDBObject(field, -1));
			} else {
				result = collection.find().sort(new BasicDBObject(field, -1));
			}
		}
		
		if(!result.cursor().hasNext()) {
			throw new ObjectNotFoundException(String.format("No max results for %s was not found", table));
		}
		return result.first();
	}
	
	@Override
	public Document min(String table, String database, String field, SearchField... fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		FindIterable<Document> result;
		
		ClientSession session= currentSession.get();
		if(fields.length > 0) {
			if(Objects.nonNull(session)) {
				result = collection.find(session, buildFilter(fields)).sort(new BasicDBObject(field, 1));
			} else {
				result = collection.find(buildFilter(fields)).sort(new BasicDBObject(field, 1));
			}
		} else {
			if(Objects.nonNull(session)) {
				result = collection.find(session).sort(new BasicDBObject(field, 1));
			} else {
				result = collection.find().sort(new BasicDBObject(field, 1));
			}
		}

		if(!result.cursor().hasNext()) {
			throw new ObjectNotFoundException(String.format("No entity %s was not found", table));
		}
		return result.first();
	}
	
	@Override
	public Long sumLongValues(String table, String database, String groupBy, SearchField... fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		AggregateIterable<Document> results;
		
		ClientSession session= currentSession.get();
		
		if(fields.length > 0) {
		
			if(Objects.nonNull(session)) {
				results = collection.aggregate(session,
					    Arrays.asList(
					        Aggregates.match(buildFilter(fields)),
					        Aggregates.group(null, Accumulators.sum("total", "$" + groupBy))
					    ));
			} else {
				results = collection.aggregate(
					    Arrays.asList(
					        Aggregates.match(buildFilter(fields)),
					        Aggregates.group(null, Accumulators.sum("total", "$" + groupBy))
					    ));
			}
			
		} else {
			if(Objects.nonNull(session)) {
				results = collection.aggregate(session,
					    Arrays.asList(
					    	Aggregates.group(null, Accumulators.sum("total", "$" + groupBy))
					    ));
			} else {
				results = collection.aggregate(
					    Arrays.asList(
					    	Aggregates.group(null, Accumulators.sum("total", "$" + groupBy))
					    ));
			}
			
		}
		
		if(!results.cursor().hasNext()) {
			return 0L;
		}
		
		Document doc = results.first();
		return doc.getLong("total");
	}
	
	@Override
	public Double sumDoubleValues(String table, String database, String groupBy, SearchField... fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		AggregateIterable<Document> results;
		
		ClientSession session= currentSession.get();
		
		if(fields.length > 0) {
		
			if(Objects.nonNull(session)) {
				results = collection.aggregate(session,
					    Arrays.asList(
					        Aggregates.match(buildFilter(fields)),
					        Aggregates.group(null, Accumulators.sum("total", "$" + groupBy))
					    ));
			} else {
				results = collection.aggregate(
					    Arrays.asList(
					        Aggregates.match(buildFilter(fields)),
					        Aggregates.group(null, Accumulators.sum("total", "$" + groupBy))
					    ));
			}
			
		} else {
			if(Objects.nonNull(session)) {
				results = collection.aggregate(session,
					    Arrays.asList(
					    	Aggregates.group(null, Accumulators.sum("total", "$" + groupBy))
					    ));
			} else {
				results = collection.aggregate(
					    Arrays.asList(
					    	Aggregates.group(null, Accumulators.sum("total", "$" + groupBy))
					    ));
			}
			
		}
		
		if(!results.cursor().hasNext()) {
			return 0D;
		}
		
		Document doc = results.first();
		return doc.getDouble("total");
	}
	
	@Override
	public Document find(String field, String value, String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		FindIterable<Document> result;
		
		ClientSession session= currentSession.get();
		if(Objects.nonNull(session)) {
			result = collection.find(session, Filters.eq(field, value))
					.collation(getCollation());
		} else {
			result = collection.find(Filters.eq(field, value))
					.collation(getCollation());
		}

		if(!result.cursor().hasNext()) {
			throw new ObjectNotFoundException(String.format("%s %s for entity %s was not found", field, value, table));
		}
		return result.first();
	}

	@Override
	public void deleteByUUID(String uuid, String table, String database) {
		
		getByUUID(uuid, table, database);
		MongoCollection<Document> collection = getCollection(table, database);
		
		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			collection.deleteOne(session, Filters.eq("_id", uuid));
		} else {
			collection.deleteOne(Filters.eq("_id", uuid));
		}
	}
	
	@Override
	public void delete(String table, String database, SearchField... fields) {

		if(deleteIfExists(table, database, fields) == 0) {
			throw new ObjectException("Object not deleted!");
		}
	}
	
	@Override
	public long deleteIfExists(String table, String database, SearchField... fields) {

		MongoCollection<Document> collection = getCollection(table, database);
		DeleteResult result;
		
		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			result = collection.deleteMany(session, buildFilter(fields));
		} else {
			result = collection.deleteMany(buildFilter(fields));
		}
		
		return result.getDeletedCount();
	}
	
	@Override
	public Iterable<Document> list(String table, String database, SearchField... fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		ClientSession session = currentSession.get();
		
		if(fields.length == 0) {
			if(Objects.nonNull(session)) {
				return collection.find(session);
			} else {
				return collection.find();
			}
		} else {
			if(Objects.nonNull(session)) {
				return collection.find(session, buildFilter(fields))
					.collation(getCollation());
			} else {
				return collection.find(buildFilter(fields))
						.collation(getCollation());
			}
		}
	}
	
	@Override
	public Iterable<Document> search(String table, String database, SearchField...fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);

		ClientSession session = currentSession.get();
		
		if(fields.length == 0) {
			if(Objects.nonNull(session)) {
				return collection.find(session);
			} else {
				return collection.find();
			}
		} else {
			if(Objects.nonNull(session)) {
				return collection.find(session, buildFilter(fields))
					.collation(getCollation());
			} else {
				return collection.find(buildFilter(fields))
						.collation(getCollation());
			}
		}
	}
	
	@Override
	public Iterable<Document> search(String table, String database, SortOrder order, String sortField, SearchField...fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		ClientSession session = currentSession.get();
		
		if(fields.length == 0) {
			if(Objects.nonNull(session)) {
				return collection.find(session)
						.sort(getOrder(order, sortField));
			} else {
				return collection.find()
						.sort(getOrder(order, sortField));
			}
		} else {
			if(Objects.nonNull(session)) {
				return collection.find(session, buildFilter(fields))
					.collation(getCollation())
					.sort(getOrder(order, sortField));
			} else {
				return collection.find(buildFilter(fields))
						.collation(getCollation())
						.sort(getOrder(order, sortField));
			}
		}

	}
	
	@Override
	public Iterable<Document> searchTable(String table, String database, int start, int length, SortOrder order, String sortField, SearchField...fields) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		
		ClientSession session = currentSession.get();
		
		if(fields.length == 0) {
			if(Objects.nonNull(session)) {
				return collection.find(session).sort(getOrder(order, sortField)).skip(start).limit(length);
			} else {
				return collection.find().sort(getOrder(order, sortField)).skip(start).limit(length);
			}
		} else {
			if(Objects.nonNull(session)) {
				return collection.find(session, buildFilter(fields))
						.collation(getCollation())
						.sort(getOrder(order, sortField)).skip(start).limit(length);
			} else {
				return collection.find(buildFilter(fields))
						.collation(getCollation())
						.sort(getOrder(order, sortField)).skip(start).limit(length);	
			}
			
		}
	}
		
	private Bson getOrder(SortOrder order, String sortField) {
		switch(order) {
		case DESC:
			return descending(sortField);
		case ASC:
		default:
			return ascending(sortField);
		}
	}

	@Override
	public Long searchCount(String table, String database, SearchField... fields) {
		MongoCollection<Document> collection = getCollection(table, database);
		
		ClientSession session = currentSession.get();
		
		if(fields.length == 0) {
			if(Objects.nonNull(session)) {
				return collection.countDocuments(session);
			} else {
				return collection.countDocuments();	
			}
		} else {
			CountOptions options = new CountOptions();
			options.collation(getCollation());
			
			if(Objects.nonNull(session)) {
				return collection.countDocuments(session, buildFilter(fields), options);	
			} else {
				return collection.countDocuments(buildFilter(fields), options);
			}
			
		}
	}
	
	@Override
	public Iterable<Document> table(String table, String searchField, String searchValue, String database, int start, int length, SortOrder order, String sortField) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		searchField = configureSearch(searchField);
		
		ClientSession session = currentSession.get();
		
		if(StringUtils.isBlank(searchValue)) {
			if(Objects.nonNull(session)) {
				return collection.find(session).sort(getOrder(order, sortField)).skip(start).limit(length);
			} else {
				return collection.find().sort(getOrder(order, sortField)).skip(start).limit(length);
			}
			
		} else {
			if(Objects.nonNull(session)) {
				return collection.find(session, Filters.regex(searchField, searchValue)).sort(getOrder(order, sortField)).skip(start).limit(length);
			} else {
				return collection.find(Filters.regex(searchField, searchValue)).sort(getOrder(order, sortField)).skip(start).limit(length);	
			}
		}
	}

	@Override
	public Long count(String table, String database, SearchField... fields) {
		MongoCollection<Document> collection = getCollection(table, database);
		
		ClientSession session = currentSession.get();
		
		if(fields.length > 0) {
		
			if(Objects.nonNull(session)) {
				CountOptions options = new CountOptions();
				options.collation(getCollation());
				return collection.countDocuments(session, buildFilter(fields), options);
			} else {
				CountOptions options = new CountOptions();
				options.collation(getCollation());
				return collection.countDocuments(buildFilter(fields), options);
			}
			
		} else {
			if(Objects.nonNull(session)) {
				return collection.countDocuments(session);
			} else {
				return collection.countDocuments();
			}
			
		}
		
	}
	
	@Override
	public Long count(String table, String searchField, String searchValue, String database) {
		MongoCollection<Document> collection = getCollection(table, database);
		searchField = configureSearch(searchField);
		
		ClientSession session = currentSession.get();
		
		if(StringUtils.isBlank(searchValue)) {
			if(Objects.nonNull(session)) {
				return collection.countDocuments(session);
			} else {
				return collection.countDocuments();	
			}
			
		} else {
			if(Objects.nonNull(session)) {
				return collection.countDocuments(session, Filters.regex(searchField, searchValue));
			} else {
				return collection.countDocuments(Filters.regex(searchField, searchValue));	
			}
			
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
		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			getCollection(table, database).drop(session);
		} else {
			getCollection(table, database).drop();
		}
	}

	@Override
	public Document getFirst(String uuid, String table, String database) {
		MongoCollection<Document> collection = getCollection(table, database);
		
		ClientSession session = currentSession.get();
		
		FindIterable<Document> result;
		
		if(Objects.nonNull(session)) {
			result = collection.find(session, Filters.eq("_id", uuid));
		} else {
			result = collection.find(Filters.eq("_id", uuid));
		}
		
		if(!result.cursor().hasNext()) {
			throw new ObjectNotFoundException(String.format("id %s for entity %s was not found", uuid, table));
		}
		return result.first();
	}

	@Override
	public void dropDatabase(String database) {
		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			mongo.getClient().getDatabase(getDatabaseName(database)).drop(session);
		} else {
			mongo.getClient().getDatabase(getDatabaseName(database)).drop();
		}
		
	}
	
	
	private void buildSearchString(SearchField.Type queryType, StringBuilder builder, SearchField[] fields) {

		boolean close = false;
		if(builder.length() > 0) {
			switch(queryType) {
			case OR:
				builder.append(" OR (");
				close = true;
				break;
			case AND:
				builder.append(" AND (");
				close = true;
				break;
			default:
				break;
			}
		}
		
		int idx = 0;
		for(SearchField field : fields) {
			if(idx++ > 0) {
				builder.append(" ");
				builder.append(queryType.name());
				builder.append(" ");
			}
			switch(field.getSearchType()) {
			case EQUALS:
				builder.append(field.getColumn());
				builder.append(" = ");
				builder.append(field.getValue()[0]);
				break;
			case ALL:
				builder.append(field.getColumn());
				builder.append(" ALL( ");
				builder.append(Utils.csv(field.getValue()));
				builder.append(" ) ");
				break;
			case IN:
				builder.append(field.getColumn());
				builder.append(" IN( ");
				builder.append(Utils.csv(field.getValue()));
				builder.append(" ) ");
				break;
			case GT:
				builder.append(field.getColumn());
				builder.append(" > ");
				builder.append(field.getValue()[0]);
				break;
			case GTE:
				builder.append(field.getColumn());
				builder.append(" >= ");
				builder.append(field.getValue()[0]);
				break;
			case LT:
				builder.append(field.getColumn());
				builder.append(" < ");
				builder.append(field.getValue()[0]);
				break;
			case LTE:
				builder.append(field.getColumn());
				builder.append(" <= ");
				builder.append(field.getValue()[0]);
				break;
			case NOT:
				builder.append(field.getColumn());
				builder.append(" != ");
				builder.append(field.getValue()[0]);
				break;
			case LIKE:
				builder.append(field.getColumn());
				builder.append(" MATCHES ");
				builder.append(Utils.csv(field.getValue()));
				break;
			case OR:
				buildSearchString(SearchField.Type.OR, builder, field.getFields());
				break;
			case AND:
				buildSearchString(SearchField.Type.AND, builder, field.getFields());
				break;
			}
		}
		
		if(close) {
			builder.append(") ");
		}
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
			case ALL:
				tmp.add(Filters.all(field.getColumn(), field.getValue()));
				break;
			case IN:
				tmp.add(Filters.in(field.getColumn(), field.getValue()));
				break;
			case NOT:
				tmp.add(Filters.ne(field.getColumn(), field.getValue()[0]));
				break;
			case LIKE:
				tmp.add(Filters.regex(field.getColumn(), field.getValue()[0].toString(), "i"));
				break;
			case GT:
				tmp.add(Filters.gt(field.getColumn(), field.getValue()[0]));
				break;
			case GTE:
				tmp.add(Filters.gte(field.getColumn(), field.getValue()[0]));
				break;
			case LT:
				tmp.add(Filters.lt(field.getColumn(), field.getValue()[0]));
				break;
			case LTE:
				tmp.add(Filters.lte(field.getColumn(), field.getValue()[0]));
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

	@Override
	public Set<String> getIndexNames(String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		Set<String> results = new HashSet<>();
		
		ListIndexesIterable<Document> indexes;
		
		indexes = collection.listIndexes();
		
		for(Document index : indexes) {
			if(!index.getString("name").equals("_id_")) {
				results.add(index.getString("name"));
			}
			System.out.println(index.toString());
		}
		return results;
	}

	@Override
	public boolean isTransactionActive() {
		ClientSession session = currentSession.get();
		if(Objects.nonNull(session)) {
			return session.hasActiveTransaction();
		}
		return false;
	}


}
