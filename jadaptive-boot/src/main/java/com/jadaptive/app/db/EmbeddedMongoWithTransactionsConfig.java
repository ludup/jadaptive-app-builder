package com.jadaptive.app.db;
import java.io.File;
import java.io.IOException;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.jadaptive.api.app.ApplicationProperties;
import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.ImmutableNet;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.process.types.ImmutableProcessConfig;
import de.flapdoodle.embed.process.types.ProcessConfig;
import de.flapdoodle.reverse.TransitionWalker.ReachedState;
import de.flapdoodle.reverse.transitions.Start;

/**
 * Class for auto-configuring and starting an embedded MongoDB with support for transactions.
 * As there's some overhead in using it and slower startup time, use it only if support for
 * transactions is needed.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ MongoClient.class })
@ConditionalOnProperty(matchIfMissing = true, name = "mongodb.embedded", havingValue = "true")
public class EmbeddedMongoWithTransactionsConfig {

	static Logger log = LoggerFactory.getLogger(EmbeddedMongoWithTransactionsConfig.class);
	
    public static final int DFLT_PORT_NUMBER = 27017;
    public static final String DFLT_REPLICASET_NAME = "rs0";
    public static final int DFLT_STOP_TIMEOUT_MILLIS = 200;

    private Version.Main mFeatureAwareVersion = Version.Main.valueOf(System.getProperty("mongodb.embeddedVersion", Version.Main.V7_0.name()));

    private String mReplicaSetName = DFLT_REPLICASET_NAME;
    //private long mStopTimeoutMillis = DFLT_STOP_TIMEOUT_MILLIS;

    File databasePath = new File(System.getProperty("user.dir"), "db");

    @Bean
    @Primary
    public Mongod mongod() throws IOException {

    	if(log.isInfoEnabled()) {
    		log.info("Starting mongod");
    	}
    	
    	if(!databasePath.exists()) {
    		databasePath.mkdirs();
    	}
    	
    	Storage storage = Storage.of(DFLT_REPLICASET_NAME, 0);
    	
    	MongodArguments mongodArguments = MongodArguments.builder()
    			.replication(storage)
    			.useNoJournal(false)
    			.build();
    	
    	
        ImmutableMongod mongod = Mongod.builder()
    		   .databaseDir(Start.to(DatabaseDir.class).initializedWith(DatabaseDir.of(databasePath.toPath())))
               .processConfig(Start.to(ProcessConfig.class).initializedWith(ImmutableProcessConfig.builder().daemonProcess(true).build()))
    		   .mongodArguments(Start.to(MongodArguments.class)
                    .initializedWith(mongodArguments))
	                .net(Start.to(Net.class)
                		.initializedWith(ImmutableNet.builder()
                				.isIpv6(false)
                				.port(ApplicationProperties.getValue("mongodb.port", DFLT_PORT_NUMBER))
                    	.bindIp(ApplicationProperties.getValue("mongodb.hostname", "127.0.0.1")).build()))
                .build();

        ReachedState<RunningMongodProcess> state = mongod.start(mFeatureAwareVersion);
        
        if(log.isInfoEnabled()) {
    		log.info("Started mongod");
    	}
        
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
        	try {
        	state.close();
        	} catch(Throwable e) { }
        }));

        File replicaSetInitiated = new File(databasePath, ".replicaSet");
    	if(!replicaSetInitiated.exists()) {
    		
    		if(log.isInfoEnabled()) {
        		log.info("Creating replica set");
        	}
        	
	        MongoClient mongoClient = null;
	        try {
	            final BasicDBList members = new BasicDBList();
	            members.add(new Document("_id", 0).append("host",
	            		ApplicationProperties.getValue("mongodb.hostname", "127.0.0.1") + ":" + 
	            		ApplicationProperties.getValue("mongodb.port", 27017)));
	
	            final Document replSetConfig = new Document("_id", mReplicaSetName);
	            replSetConfig.put("members", members);
	
	            mongoClient =
	                new MongoClient(new ServerAddress(
	                		ApplicationProperties.getValue("mongodb.hostname", "127.0.0.1"), 
	                		ApplicationProperties.getValue("mongodb.port", 27017)));
	            final MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
	            adminDatabase.runCommand(new Document("replSetInitiate", replSetConfig));
	
	            if(log.isInfoEnabled()) {
	        		log.info("Created replica set");
	        	}
	        	
	            replicaSetInitiated.createNewFile();
	        }
	        finally {
	            if (mongoClient != null) {
	                mongoClient.close();
	            }
	        }
    	}
        
        return mongod;
     }

}