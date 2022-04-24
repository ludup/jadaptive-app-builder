package com.jadaptive.plugins.email;

import org.simplejavamail.api.mailer.config.TransportStrategy;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = SMTPConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
@ObjectViews({@ObjectViewDefinition(value = SMTPConfiguration.SERVER_VIEW, bundle = SMTPConfiguration.RESOURCE_KEY),
				@ObjectViewDefinition(value = SMTPConfiguration.CREDENTIALS_VIEW, bundle = SMTPConfiguration.RESOURCE_KEY), 
				@ObjectViewDefinition(value = SMTPConfiguration.DELIVERY_VIEW, bundle = SMTPConfiguration.RESOURCE_KEY)})
public class SMTPConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = -2175601630716215887L;

	public static final String RESOURCE_KEY = "smtpConfiguration";
	public static final String SERVER_VIEW = "smtpServer";
	public static final String CREDENTIALS_VIEW = "smtpCredentials";
	public static final String DELIVERY_VIEW = "smtpDelivery";
	
	@ObjectField(type = FieldType.BOOL)
	Boolean enabled;
	
	@ObjectField(type = FieldType.ENUM)
	@ObjectView(SERVER_VIEW)
	TransportStrategy protocol;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(SERVER_VIEW)
	String hostname;
	
	@ObjectField(type = FieldType.INTEGER)
	@ObjectView(SERVER_VIEW)
	int port;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(CREDENTIALS_VIEW)
	String username;
	
	@ObjectField(type = FieldType.PASSWORD, manualEncryption = true)
	@ObjectView(CREDENTIALS_VIEW)
	String password;

	@ObjectField(type = FieldType.TEXT)
	@ObjectView(DELIVERY_VIEW)
	String fromName;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(DELIVERY_VIEW)
	String fromAddress;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(DELIVERY_VIEW)
	String replyToName;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(DELIVERY_VIEW)
	String replyToAddress;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(DELIVERY_VIEW)
	String archiveAddress;
	
	public Boolean getEnabled() {
		return enabled == null ? Boolean.FALSE : enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public TransportStrategy getProtocol() {
		return protocol;
	}
	
	public void setProtocol(TransportStrategy protocol) {
		this.protocol = protocol;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getArchiveAddress() {
		return archiveAddress;
	}

	public void setArchiveAddress(String archiveAddress) {
		this.archiveAddress = archiveAddress;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	
	public String getReplyToAddress() {
		return replyToAddress;
	}

	public void setReplyToAddress(String replyToAddress) {
		this.replyToAddress = replyToAddress;
	}

	public String getReplyToName() {
		return replyToName;
	}

	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}
