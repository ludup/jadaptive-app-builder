package com.jadaptive.app.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.utils.Utils;
import com.mongodb.client.ClientSession;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

public class AbstractTenantAwareDatabase {

	private static Logger log = LoggerFactory.getLogger(AbstractTenantAwareDatabase.class);
	
	private ThreadLocal<Transaction> currentSession = new ThreadLocal<>();

	@Autowired
	protected MongoDatabaseService mongo;

	protected MongoCollection<Document> getCollection(String table, String database) {
		MongoDatabase db = mongo.getClient(database).getDatabase(getDatabaseName(database));
		return db.getCollection(table);
	}
	
	protected MongoDatabase getDatabase(String database) {
		return mongo.getClient(database).getDatabase(getDatabaseName(database));
	}
	
	protected String getDatabaseName(String database) {
		String prefix = ApplicationProperties.getValue("mongodb.databasePrefix", null);
		if(Objects.nonNull(prefix)) {
			return String.format("%s-%s", prefix, database);
		}
		return database;
	}
	
	protected Transaction getCurrentSession() {
		return currentSession.get();
	}

	public void doInTransaction(String database, Runnable r) {
		
		if(isTransactionActive()) {
			r.run();
		} else {
			
			if(Boolean.getBoolean("jadaptive.logTransactions") && log.isInfoEnabled()) {
				log.info("TRANSACTION: Starting transaction");
			}
			
			currentSession.set(new Transaction(database));
			
			try {
				r.run();
				if(Boolean.getBoolean("jadaptive.logTransactions") && log.isInfoEnabled()) {
					log.info("TRANSACTION: Committing transaction");
				}
				currentSession.get().commitTransaction();
			} catch(Throwable t) {
				if(Boolean.getBoolean("jadaptive.logTransactions") && log.isInfoEnabled()) {
					log.info("TRANSACTION: Aborting transaction");
				}
				currentSession.get().abortTransaction();
				if(t instanceof RuntimeException) {
					throw t;
				}
				throw new IllegalStateException("Transaction failed with " + t.getMessage(), t);
			} finally {
				if(Boolean.getBoolean("jadaptive.logTransactions") && log.isInfoEnabled()) {
					log.info("TRANSACTION: Closing session");
				}
				currentSession.get().close();
				currentSession.remove();
			}
		}
	}

	public void createTextIndex(String fieldName, String table, String database) {
		String indexName = "text_" + fieldName;
		MongoCollection<Document> collection = getCollection(table, database);
		IndexOptions indexOptions = new IndexOptions()
				.collation(getCollation())
				.name(indexName);
		Transaction transaction = currentSession.get();
		if(Objects.nonNull(transaction)) {
			collection.createIndex(transaction.session(database), Indexes.text(fieldName), indexOptions);
		} else {
			collection.createIndex(Indexes.text(fieldName), indexOptions);		
		}
	}
	
	protected Collation getCollation() {
		return Collation.builder().collationStrength(CollationStrength.PRIMARY)
				.locale(Locale.getDefault().getLanguage())
				.build();
	}

	public void createIndex(String table, String database, String... fieldNames) {
		String indexName = "index_" + StringUtils.join(fieldNames, "_");
		MongoCollection<Document> collection = getCollection(table, database);
		IndexOptions indexOptions = new IndexOptions()
				.collation(getCollation())
				.name(indexName);
		
		Transaction transaction = currentSession.get();
		if(Objects.nonNull(transaction)) {
			collection.createIndex(transaction.session(database), Indexes.ascending(fieldNames), indexOptions);
		} else {
			collection.createIndex(Indexes.ascending(fieldNames), indexOptions);		
		}
	}
	
	public void dropIndexes(String table, String database) {
		MongoCollection<Document> collection = getCollection(table, database);

		Transaction transaction = currentSession.get();
		if(Objects.nonNull(transaction)) {
			collection.dropIndexes(transaction.session(database));
		} else {
			collection.dropIndexes();	
		}
	}
	
	public void createUniqueIndex(String table, String database, String...fieldNames) {
		String indexName = "unique_" + StringUtils.join(fieldNames, "_");
		MongoCollection<Document> collection = getCollection(table, database);
		IndexOptions indexOptions = new IndexOptions()
				.collation(Collation.builder().collationStrength(CollationStrength.PRIMARY)
						.locale(Locale.getDefault().getLanguage())
						.caseLevel(false).build())
				.unique(true)
				.name(indexName);
		
		Transaction transaction = currentSession.get();
		if(Objects.nonNull(transaction)) {
			collection.createIndex(transaction.session(database), Indexes.ascending(fieldNames), indexOptions);
		} else {
			collection.createIndex(Indexes.ascending(fieldNames), indexOptions);
		}
	}
	
	public Set<String> getIndexNames(String table, String database) {
		
		MongoCollection<Document> collection = getCollection(table, database);
		Set<String> results = new HashSet<>();
		
		ListIndexesIterable<Document> indexes;
		
		indexes = collection.listIndexes();
		
		for(Document index : indexes) {
			if(!index.getString("name").equals("_id_")) {
				results.add(index.getString("name"));
			}
		}
		return results;
	}

	public boolean isTransactionActive() {
		Transaction session = currentSession.get();
		if(Objects.nonNull(session)) {
			return session.hasActiveTransaction();
		}
		return false;
	}

	class Transaction {
		
		String id = Utils.generateRandomAlphaNumericString(8);
		Map<String,ClientSession> databaseSessions = new HashMap<>();
		
		public Transaction(String database) {
			startTransaction(database);
		}

		public boolean hasActiveTransaction() {
			return !databaseSessions.isEmpty();
		}
		
		public ClientSession session(String database) {
			ClientSession session = databaseSessions.get(database);
			if(session==null) {
				return startTransaction(database);
			}
			return session;
		}

		public void close() {
			for(ClientSession session : databaseSessions.values()) {
				session.close();
			}
			databaseSessions.clear();
		}

		public void abortTransaction() {
			for(ClientSession session : databaseSessions.values()) {
				session.abortTransaction();
			}
		}

		public void commitTransaction() {
			for(ClientSession session : databaseSessions.values()) {
				session.commitTransaction();
			}
		}

		public ClientSession startTransaction(String database) {
			if(Boolean.getBoolean("jadaptive.logTransactions") && log.isInfoEnabled()) {
				log.info("TRANSACTION: Creating session for transaction {} on database {}", id, database);
			}
			ClientSession session = mongo.getClient(database).startSession();
			databaseSessions.put(database, session);
			session.startTransaction();
			return session;
		}
		
	}
}
