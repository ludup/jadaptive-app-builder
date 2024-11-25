package com.jadaptive.app.db;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	
	private Map<String,MongoClient> mongoClients = Collections.synchronizedMap(new HashMap<>());
	
	@Override
	public synchronized MongoClient getClient(String uuid) {
		try {
			if(!mongoClients.containsKey(uuid)) {
				return connect(uuid);
			}
			return mongoClients.get(uuid);
		} catch (IOException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected MongoClient connect(String uuid) throws IOException {
		
		String connectionString = ApplicationProperties.getValue(String.format("mongodb.%s.connection", uuid), null);
		if(StringUtils.isBlank(connectionString)) {
			connectionString =  ApplicationProperties.getValue(String.format("mongodb.connection", uuid), null);
		}
		
		if(StringUtils.isNotBlank(connectionString)) {
			return new MongoClient(new MongoClientURI(connectionString));
		} else {
			return new MongoClient(
				ApplicationProperties.getValue("mongodb.hostname", "localhost"),
				ApplicationProperties.getValue("mongodb.port", 27017));
		}
	}

}
