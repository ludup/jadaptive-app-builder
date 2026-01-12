package com.jadaptive.api.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationProperties {

	
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
		File certificateProperties = new File(confdFolder, "certificate.properties");
		File serverProperties = new File(confdFolder, "server.properties");
		
		if(!certificateProperties.exists()) {
			try {
				
				FileUtils.writeStringToFile(certificateProperties,"""		
				# Certificate Properties
				server.ssl.bundle=default
				spring.ssl.bundle.jks.default.reload-on-update=true
				spring.ssl.bundle.jks.default.key.alias=server
				spring.ssl.bundle.jks.default.keystore.location=conf.d/default/cert.p12
				spring.ssl.bundle.jks.default.keystore.password=changeit
				spring.ssl.bundle.jks.default.keystore.type=PKCS12

				""", Charset.forName("UTF-8"));
				
			} catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
		if(propertiesFile.exists()) {
			
			if(!serverProperties.exists()) {
				try {
					Properties props = new Properties();
					try(InputStream in = new FileInputStream(propertiesFile)) {
						props.load(in);
					}
					
					Properties newProps = new Properties();
					newProps.setProperty("server.port", props.getProperty("server.port"));
					newProps.setProperty("server.ssl.enabled", "true");
					newProps.setProperty("server.ssl.protocol", "TLS");
					
					try(OutputStream out = new FileOutputStream(serverProperties)) {
						newProps.store(out, "Server Properties");
					}
	
				} catch(IOException e ) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			
			if(!propertiesFile.delete()) {
				throw new IllegalStateException("Cannot delete conf.d/ssl.properties! Please delete this file manually");
			}
		} else if(!serverProperties.exists()) {
			
			try {
				FileUtils.writeStringToFile(serverProperties,"""		
						# Server Properties
						server.port=443
						server.ssl=true
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
				#mongodb.connection=
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
		System.setProperty("log4j.configurationFile", log4j.getAbsolutePath());
		
		
		properties = new Properties();
		
		Logger log = LoggerFactory.getLogger(ApplicationProperties.class);
		for(File file : Arrays.asList(confdFolder.listFiles(f -> f.isFile() && f.getName().endsWith(".properties"))).stream().sorted().toList()) {
			log.info("Loading properties file {}", file.getName());
			try {
				properties.putAll(loadPropertiesFile(file));
			} catch (IOException e) {
				log.error("Faild to load properties file {}", file.getName(), e);
			}
		}
		
		checkLoaded(log);
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


	private static void checkLoaded(Logger log) {
		if(Objects.isNull(properties)) {
			log.warn("No jadaptive.properties has been loaded. Using application defaults");
		}
	}

	public static File getConfdFolder() {
		return confdFolder;
	}


	
	
}
