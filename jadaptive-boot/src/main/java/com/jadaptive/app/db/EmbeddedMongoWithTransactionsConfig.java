package com.jadaptive.app.db;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.bson.Document;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.jadaptive.api.app.ApplicationProperties;
import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongoCmdOptions;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Class for auto-configuring and starting an embedded MongoDB with support for transactions.
 * As there's some overhead in using it and slower startup time, use it only if support for
 * transactions is needed.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({ MongoAutoConfiguration.class })
@ConditionalOnClass({ MongoClient.class, MongodStarter.class })
@ConditionalOnProperty(matchIfMissing = true, name = "mongodb.embedded", havingValue = "true")
@Import({ 
    EmbeddedMongoAutoConfiguration.class,
    EmbeddedMongoWithTransactionsConfig.DependenciesConfiguration.class 
})
public class EmbeddedMongoWithTransactionsConfig {

   // public static final int DFLT_PORT_NUMBER = 27017;
    public static final String DFLT_REPLICASET_NAME = "rs0";
    public static final int DFLT_STOP_TIMEOUT_MILLIS = 200;

    private Version.Main mFeatureAwareVersion = Version.Main.V4_4;
    //private int mPortNumber = DFLT_PORT_NUMBER;
    private String mReplicaSetName = DFLT_REPLICASET_NAME;
    private long mStopTimeoutMillis = DFLT_STOP_TIMEOUT_MILLIS;

    File databasePath = new File(System.getProperty("user.dir"), "db");
    
    @Bean
    public MongodConfig mongodConfig() throws UnknownHostException, IOException {
    	
    	Storage storage = new Storage(databasePath.getAbsolutePath(), DFLT_REPLICASET_NAME, 0);
    	 
    	MongodConfig mongodConfig = MongodConfig.builder()
    		    .version(mFeatureAwareVersion)
    		    .replication(storage)
    		    .net(new Net(ApplicationProperties.getValue("mongodb.port", 27017), Network.localhostIsIPv6()))
    		    .cmdOptions(MongoCmdOptions.builder()
    		            .useNoJournal(false)
    		            .build())
    		    .stopTimeoutInMillis(mStopTimeoutMillis)
    		    .build();
    	
        return mongodConfig;
    }

    /**
     * Initializes a new replica set.
     * Based on code from https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/257
     */
    class EmbeddedMongoReplicaSetInitialization {

        EmbeddedMongoReplicaSetInitialization() throws Exception {
        	
        	File replicaSetInitiated = new File(databasePath, ".replicaSet");
        	if(replicaSetInitiated.exists()) {
        		return;
        	}
            MongoClient mongoClient = null;
            try {
                final BasicDBList members = new BasicDBList();
                members.add(new Document("_id", 0).append("host", "localhost:" + 
                		ApplicationProperties.getValue("mongodb.port", 27017)));

                final Document replSetConfig = new Document("_id", mReplicaSetName);
                replSetConfig.put("members", members);

                mongoClient =
                    new MongoClient(new ServerAddress(Network.getLocalHost(), 
                    		ApplicationProperties.getValue("mongodb.port", 27017)));
                final MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
                adminDatabase.runCommand(new Document("replSetInitiate", replSetConfig));

                replicaSetInitiated.createNewFile();
            }
            finally {
                if (mongoClient != null) {
                    mongoClient.close();
                }
            }
        }
    }

    @Bean
    EmbeddedMongoReplicaSetInitialization embeddedMongoReplicaSetInitialization() throws Exception {
        return new EmbeddedMongoReplicaSetInitialization();
    }

    /**
     * Additional configuration to ensure that the replica set initialization happens after the
     * {@link MongodExecutable} bean is created. That's it - after the database is started.
     */
    @ConditionalOnClass({ MongoClient.class, MongodStarter.class })
    protected static class DependenciesConfiguration
        extends AbstractDependsOnBeanFactoryPostProcessor {

        DependenciesConfiguration() {
            super(EmbeddedMongoReplicaSetInitialization.class, null, MongodExecutable.class);
        }
    }

}