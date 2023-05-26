package com.jadaptive.app.db;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.repository.RepositoryException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


@Service
public class MongoDatabaseServiceImpl implements MongoDatabaseService {

	static Logger log = LoggerFactory.getLogger(MongoDatabaseServiceImpl.class);
	
	private MongoClient mongoClient;
	 
	@Override
	public synchronized MongoClient getClient() {
		try {
			if(mongoClient==null) {
				connect();
			}
			return mongoClient;
		} catch (IOException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected void connect() throws IOException {
		
		String connectionString = ApplicationProperties.getValue("mongodb.connection", null);
		
		if(StringUtils.isNotBlank(connectionString)) {
			mongoClient = new MongoClient(new MongoClientURI(connectionString));
		} else {
			mongoClient = new MongoClient(
				ApplicationProperties.getValue("mongodb.hostname", "localhost"),
				ApplicationProperties.getValue("mongodb.port", 27017));
		}
	}

}
