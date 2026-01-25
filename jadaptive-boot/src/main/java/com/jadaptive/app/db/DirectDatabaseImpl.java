package com.jadaptive.app.db;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.DirectDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;

@Repository
public class DirectDatabaseImpl extends AbstractTenantAwareDatabase implements DirectDatabase {

static Logger log = LoggerFactory.getLogger(DirectDatabaseImpl.class);
	
	@Autowired
	protected MongoDatabaseService mongo;
	
	
	
	@Override
	public long getNextSequence(String table, String database, String sequenceName) {
       
		MongoCollection<Document> collection = getCollection(table, database);
		
		// 1. Identify which sequence document to update
        Bson filter = Filters.eq("_id", sequenceName);

        // 2. Define the atomic increment ($inc)
        Bson update = Updates.inc("currentValue", 1L);

        // 3. Configure options:
        // - upsert: true (create the document if it doesn't exist)
        // - returnDocument: AFTER (give us the incremented value, not the old one)
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);

        // 4. Execute atomically
        Document result = collection.findOneAndUpdate(filter, update, options);

        return result.getLong("currentValue");
    }
	
//	@Override
//	public void watch(String table, String database, Consumer<ChangeStreamDocument<Document>> consumer) {
//
//		var collection = getCollection(table, database);
//		// TOOD what to do for this?
////		        ,  new FindOptions().setMaxAwaitTime(60, TimeUnit.SECONDS)
//		while(true) {
//			try { 
//				collection.watch(/* pipeline */).forEach(consumer);
//			}
//			catch(MongoQueryException mqe) {
//				try {
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					throw new IllegalStateException(e);
//				}
//			}
//		}
//		
//	}
}
