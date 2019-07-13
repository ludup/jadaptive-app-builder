package com.jadaptive.db;

import com.mongodb.MongoClient;

public interface MongoDatabaseService {

	public MongoClient getClient();
}
