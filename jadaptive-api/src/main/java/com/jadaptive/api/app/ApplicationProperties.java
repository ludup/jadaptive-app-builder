package com.jadaptive.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationProperties {

	Logger log = LoggerFactory.getLogger(ApplicationProperties.class);
	
	static ApplicationProperties instance = new ApplicationProperties();
	static Properties properties;
	
	ApplicationProperties() {
		properties = new Properties();
		try(InputStream in = new FileInputStream(
				new File(System.getProperty("jadaptive.conf", "conf"), 
						"jadaptive.properties"))) {
			properties.load(in);
		} catch(IOException e) {
			log.error("Could not load database properties", e);
		}
		checkLoaded();
	}
	
	public static String getValue(String name, String defaultValue) {
		checkLoaded();
		return properties.getProperty(name, defaultValue);
	}
	
	public static boolean getValue(String name, boolean defaultValue) {
		checkLoaded();
		String val = properties.getProperty(name);
		if(Objects.isNull(val)) {
			return defaultValue;
		}
		return Boolean.parseBoolean(val);
	}
	
	public static int getValue(String name, int defaultValue) {
		checkLoaded();
		String val = properties.getProperty(name);
		if(Objects.isNull(val)) {
			return defaultValue;
		}
		return Integer.parseInt(val);
	}


	private static void checkLoaded() {
		if(Objects.isNull(properties)) {
			throw new IllegalStateException("jadaptive.properties could not be loaded");
		}
	}


	
	
}
