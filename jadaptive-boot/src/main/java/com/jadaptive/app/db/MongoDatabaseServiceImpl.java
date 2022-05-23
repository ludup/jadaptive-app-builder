package com.jadaptive.app.db;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.repository.RepositoryException;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodProcess;


@Service
public class MongoDatabaseServiceImpl implements MongoDatabaseService {

	static Logger log = LoggerFactory.getLogger(MongoDatabaseServiceImpl.class);
	
	private MongoClient mongoClient;
	private MongodProcess process = null;
	
//	@PostConstruct
//	public void init() throws IOException {
//		
//		if(ApplicationProperties.getValue("mongodb.embedded", true)) {
//		    Storage storage = new Storage(
//		            System.getProperty("user.dir") + "/db", null, 0);
//	
//		    MongodStarter starter = MongodStarter.getDefaultInstance();
//
//		    int port = ApplicationProperties.getValue("mongodb.port", 27017);
//		    
//		    MongodConfig mongodConfig = MongodConfig.builder()
//		        .version(Version.Main.PRODUCTION)
//		        .replication(storage)
//		        .net(new Net(port, Network.localhostIsIPv6()))
//		        .build();
//		    
////		    IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
////		            .defaults(Command.MongoD)
////		            .artifactStore(new ExtractedArtifactStoreBuilder()
////		                    .defaults(Command.MongoD)
////		                    .download(new DownloadConfigBuilder()
////		                            .defaultsForCommand(Command.MongoD).build())
////		                    .executableNaming(new UUIDTempNaming())).build();
////	
////		    IMongodConfig mongodConfig = new MongodConfigBuilder()
////		            .version(Version.Main.PRODUCTION)
////		            .net(new Net(ApplicationProperties.getValue("mongodb.hostname", "localhost"), 
////		            		ApplicationProperties.getValue("mongodb.port", 27017), false))
////		            .replication(storage)
////		            .cmdOptions(new MongoCmdOptionsBuilder().defaultSyncDelay().build())
////		            .build();
//	
//		    MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
//		    process = mongodExecutable.start();
//		    
//		    Runtime.getRuntime().addShutdownHook(new Thread() {
//		    	public void run() {
//		    		if(Objects.nonNull(process)) {
//		   			 process.stop();
//		   		 }
//		    	}
//		    });
//		}
//	 }

	 @PreDestroy
	 public void stop(){
		 if(Objects.nonNull(process)) {
			 process.stop();
		 }
	 }
	 
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
		
		mongoClient = new MongoClient(
				ApplicationProperties.getValue("mongodb.hostname", "localhost"),
				ApplicationProperties.getValue("mongodb.port", 27017));
	}

}
