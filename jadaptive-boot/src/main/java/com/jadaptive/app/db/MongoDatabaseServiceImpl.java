package com.jadaptive.app.db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.tenant.TenantService;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


@Service
public class MongoDatabaseServiceImpl implements MongoDatabaseService {

	static Logger log = LoggerFactory.getLogger(MongoDatabaseServiceImpl.class);
	
	private Map<String,MongoClient> databaseClients = new HashMap<>();
	
	@Autowired
	private TenantService tenantService;
	
	@Override
	public synchronized MongoClient getClient(String database) {
		try {
			MongoClient c = databaseClients.get(database);
			if(c==null) {
				c = connect(database);
				databaseClients.put(database, c);
			}
			return c;
		} catch (IOException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}
	
	protected MongoClient connect(String database) throws IOException {
		
		String defaultConnection = ApplicationProperties.getValue("mongodb.connection", "mongodb://localhost:27017/");
		
		if(!TenantService.SYSTEM_UUID.equals(database)) {
			defaultConnection = tenantService.getTenantDatabaseConnection(database, defaultConnection); 
		}
		return new MongoClient(new MongoClientURI(defaultConnection));
	}

}
