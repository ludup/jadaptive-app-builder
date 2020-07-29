package com.jadaptive.plugins.universal;

import java.util.Properties;

import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = UACredentials.RESOURCE_KEY)
public class UACredentials extends PersonalUUIDEntity {

	private static final long serialVersionUID = 8682975909935212410L;

	public static final String RESOURCE_KEY = "uaCredentials";
	
	@ObjectField(type = FieldType.TEXT)
	String username;
	
	@ObjectField(type = FieldType.TEXT)
	String authorization;
	
	@ObjectField(type = FieldType.TEXT)
	String deviceName;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	String privateKey;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	String publicKey;
	
	@ObjectField(type = FieldType.TEXT)
	String hostname;
	
	@ObjectField(type = FieldType.INTEGER)
	int port;
	
	@ObjectField(type = FieldType.BOOL)
	Boolean strictSSL;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Boolean getStrictSSL() {
		return strictSSL;
	}

	public void setStrictSSL(Boolean strictSSL) {
		this.strictSSL = strictSSL;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public void fromProperties(Properties properties) {
		
		username = properties.getProperty("username");
		authorization = properties.getProperty("authorization");
		deviceName = properties.getProperty("deviceName");
		privateKey = properties.getProperty("privateKey");
		publicKey = properties.getProperty("publicKey");
		hostname = properties.getProperty("hostname");
		port = Integer.parseInt(properties.getProperty("port"));
		strictSSL = Boolean.valueOf(properties.getProperty("strictSSL"));
	
	}
	
	
	public Properties toProperties() {
		
		Properties properties = new Properties();
		properties.put("username", username);
		properties.put("authorization", authorization);
		properties.put("deviceName", deviceName);
		properties.put("privateKey", privateKey);
		properties.put("publicKey", publicKey);
		properties.put("hostname", hostname);
		properties.put("port", String.valueOf(port));
		properties.put("strictSSL", String.valueOf(strictSSL));
		
		return properties;
	}
}
