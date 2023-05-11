package com.jadaptive.api.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationProperties {

	static Logger log = LoggerFactory.getLogger(ApplicationProperties.class);
	
	static ApplicationProperties instance = new ApplicationProperties();
    static Properties properties;
	static File confFolder;
	static File confdFolder;
	
	ApplicationProperties() {
		
		checkBouncyCastleProvider();
		
		confFolder = new File(System.getProperty("jadaptive.conf", "conf"));
		confFolder.mkdirs();
		
		confdFolder = new File(System.getProperty("jadaptive.conf", "conf.d"));
		confdFolder.mkdirs();
		
		File propertiesFile = new File(confdFolder, "ssl.properties");
		if(!propertiesFile.exists()) {
			try {
				
				FileUtils.writeStringToFile(propertiesFile,"""		
				# Server Properties
				server.port=8443

				server.ssl.enabled=true
				server.ssl.protocol=TLS

				server.ssl.key-store-password=changeit
				server.ssl.key-store-type=PKCS12
				server.ssl.key-store=conf.d/cert.p12
				server.ssl.key-alias=server

				# PEM files will be automatically converted to PKCS12 keystore as required. 
				# If you configure PEM below the keystore above will be created with the PEM
				# certificate and keys imported into it.

				#server.ssl.private-key=conf.d/key.pem
				#server.ssl.passprase=password
				#server.ssl.ca-bundle=conf.d/chain.pem
				#server.ssl.certificate=conf.d/cert.pem
				""", Charset.forName("UTF-8"));
				
			} catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
		propertiesFile = new File(confdFolder, "database.properties");
		if(!propertiesFile.exists()) {
			try {
				
				FileUtils.writeStringToFile(propertiesFile,"""		
				# Database Properties
				#mongodb.embedded=true
				#mongodb.hostname=localhost
				#mongodb.port=27017
				""", Charset.forName("UTF-8"));
				
			} catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
		File log4j = new File(confdFolder, "log4j2.xml");
		if(!log4j.exists()) {
			try {
			FileUtils.writeStringToFile(log4j,"""	
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" monitorInterval="30">
    <Properties>
        <Property name="LOG_PATTERN">
            %d{yyyy-MM-dd HH:mm:ss.SSS} %5p --- [%15.15t] %-40.40c{1.} : %m%n%ex
        </Property>
    </Properties>
    <Appenders>
        <Console name="ConsoleAppender" target="SYSTEM_OUT">
            <PatternLayout pattern="${LOG_PATTERN}"/>
        </Console>
		<RollingFile name="FileAppender" fileName="logs/application.log"
		         filePattern="application-%i.log.gz">
		    <PatternLayout>
		        <Pattern>${LOG_PATTERN}</Pattern>
		    </PatternLayout>
		    <Policies>
		        <SizeBasedTriggeringPolicy size="10MB" />
		    </Policies>
		    <DefaultRolloverStrategy max="10"/>
		</RollingFile>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="FileAppender" />
            <AppenderRef ref="ConsoleAppender"/>
        </Root>
    </Loggers>
</Configuration>

				""", Charset.forName("UTF-8"));
			} catch(IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
			
			
		try{
			log.info("Loading properties file jadaptive.properties");
			properties = loadPropertiesFile(propertiesFile);
		} catch(IOException e) {
			log.warn("Could not load jadaptive.properties file [{}]", e.getMessage());
		}

		File confd = new File("conf.d");
		if(confd.exists()) {
			for(File file : confd.listFiles()) {
				if(file.isFile() && file.getName().endsWith(".properties")) {
					log.info("Loading extended properties file {}", file.getName());
					try {
						properties.putAll(loadPropertiesFile(file));
					} catch (IOException e) {
						log.error("Faild to load properties file {}", file.getName(), e);
					}
				}
			}
		}
		checkLoaded();
	}
	
	private void checkBouncyCastleProvider() {
		
		Provider provider = Security.getProvider("BC");
		if(Objects.isNull(provider)) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	public static Properties loadPropertiesFile(File propertiesFile) throws IOException {
		Properties properties = new Properties();
		try(InputStream in = new FileInputStream(propertiesFile)) {
			properties.load(in);
		} 
		return properties;
	}
	
	public static Properties getProperties() {
		return properties;
	}
	
	public static File getConfFolder() {
		return confFolder;
	}
	
	public static String getValue(String name, String defaultValue) {
		if(Objects.isNull(properties)) {
			return defaultValue;
		}
		String val = properties.getProperty(name);
		if(Objects.isNull(val)) {
			val = System.getProperty(name);
			if(Objects.isNull(val)) {
				return defaultValue;
			}
		}
		return val;
	}
	
	public static boolean getValue(String name, boolean defaultValue) {
		if(Objects.isNull(properties)) {
			return defaultValue;
		}
		String val = properties.getProperty(name);
		if(Objects.isNull(val)) {
			val = System.getProperty(name);
			if(Objects.isNull(val)) {
				return defaultValue;
			}
		}
		return Boolean.parseBoolean(val);
	}
	
	public static int getValue(String name, int defaultValue) {
		if(Objects.isNull(properties)) {
			return defaultValue;
		}
		String val = properties.getProperty(name);
		if(Objects.isNull(val)) {
			val = System.getProperty(name);
			if(Objects.isNull(val)) {
				return defaultValue;
			}
		}
		return Integer.parseInt(val);
	}


	private static void checkLoaded() {
		if(Objects.isNull(properties)) {
			log.warn("No jadaptive.properties has been loaded. Using application defaults");
		}
	}

	public static File getConfdFolder() {
		return confdFolder;
	}


	
	
}
