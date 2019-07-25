package com.jadaptive.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
		
		Properties properties = new Properties();
		try(InputStream in = new FileInputStream(
				new File(System.getProperty("jadaptive.conf", "conf"), 
						"database.properties"))) {
			properties.load(in);
		} catch(IOException e) {
			log.error("Could not load database properties", e);
		}
		
		mongoClient = new MongoClient(
				properties.getProperty("hostname", "localhost"),
				Integer.parseInt(properties.getProperty("port", "32768")));
	}

}
