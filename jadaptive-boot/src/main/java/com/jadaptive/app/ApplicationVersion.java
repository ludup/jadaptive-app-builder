package com.jadaptive.app;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class ApplicationVersion {

	static String version;
	
	public static String getVersion() {
		return getVersion("jadaptive-boot");
	}
	
	public static String getSerial() {
		Preferences pref = Preferences.userNodeForPackage(ApplicationVersion.class);
		
		String hypersocketId = System.getProperty("jadaptive.id", "jadaptive-unknown");
		if(pref.get("jadaptive.serial", null)!=null) {
			pref.put(hypersocketId, pref.get("jadaptive.serial", UUID.randomUUID().toString()));
			pref.remove("jadaptive.serial");
		} 
		String serial = pref.get(hypersocketId, UUID.randomUUID().toString());
		pref.put(hypersocketId, serial);
		return serial;
	}
	
	public static String getVersion(String artifactId) {
		String fakeVersion = System.getProperty("jadaptive.development.version");
		if(fakeVersion != null) {
			return fakeVersion;
		}
		
	    if (version != null) {
	        return version;
	    }

	    // try to load from maven properties first
	    try {
	        Properties p = new Properties();
	        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/META-INF/maven/com.jadaptive/" + artifactId + "/pom.properties");
	        if (is != null) {
	            p.load(is);
	            version = p.getProperty("version", "");
	        }
	    } catch (Exception e) {
	        // ignore
	    }

	    // fallback to using Java API
	    if (version == null) {
	        Package aPackage = ApplicationVersion.class.getPackage();
	        if (aPackage != null) {
	            version = aPackage.getImplementationVersion();
	            if (version == null) {
	                version = aPackage.getSpecificationVersion();
	            }
	        }
	    }

	    if (version == null) {
	    	try {
	    		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	            Document doc = docBuilder.parse (new File("pom.xml"));
	            version = doc.getDocumentElement().getElementsByTagName("version").item(0).getTextContent();
	    	} catch (Exception e) {
				version = "DEV_VERSION";
			} 
	        
	    }

	    return version;
	}

	public static String getProductId() {
		return System.getProperty("jadaptive.id", "jadaptive-basecamp");
	} 
	
	public static String getBrandId() {
		String id = getProductId();
		int idx = id.indexOf('-');
		if(idx==-1) {
			throw new IllegalStateException("Product id must consist of string formatted like <brand>-<product>");
		}
		return id.substring(0, idx);
	} 
}