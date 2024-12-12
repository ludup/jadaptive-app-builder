package com.jadaptive.app.db;

import com.mongodb.MongoClient;

public interface MongoDatabaseService {

	MongoClient getClient(String database);

}
