package com.jadaptive.db;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jadaptive.app.ApplicationProperties;
import com.jadaptive.repository.RepositoryException;
import com.mongodb.MongoClient;


@Service
public class MongoDatabaseServiceImpl implements MongoDatabaseService {

	static Logger log = LoggerFactory.getLogger(MongoDatabaseServiceImpl.class);
	
	MongoClient mongoClient;
	
	@Override
	public MongoClient getClient() {
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
		
		mongoClient = new MongoClient(
				ApplicationProperties.getValue("hostname", "localhost"),
				ApplicationProperties.getValue("port", 32768));
	}

}
