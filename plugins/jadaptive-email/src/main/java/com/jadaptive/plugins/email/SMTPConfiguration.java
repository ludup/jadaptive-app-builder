package com.jadaptive.plugins.email;

import org.codemonkey.simplejavamail.TransportStrategy;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "SMTP Configuration", resourceKey = SMTPConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
public class SMTPConfiguration extends UUIDEntity{

	public static final String RESOURCE_KEY = "smtpConfiguration";
	
	@Column(name = "Enabled", defaultValue="false", description = "Enable the SMTP email service", type = FieldType.BOOL)
	Boolean enabled;
	
	@Column(name = "Protocol", defaultValue= "SMTP_PLAIN", description = "The transport protocol to use to connect to the SMTP server", type = FieldType.ENUM)
	TransportStrategy protocol;
	
	@Column(name = "Hostname", defaultValue="localhost", description = "The hostname of the SMTP server", type = FieldType.TEXT)
	String hostname;
	
	@Column(name = "Port", defaultValue="25", description = "The port to use", type = FieldType.TEXT)
	int port;
	
	@Column(name = "Username", description = "The username for SMTP authentication", type = FieldType.TEXT)
	String username;
	
	@Column(name = "Password", description = "The password for SMTP authentication", type = FieldType.PASSWORD, manualEncryption = true)
	String password;

	@Column(name = "From Name", description = "The name of the user to place in the From field", type = FieldType.TEXT)
	String fromName;
	
	@Column(name = "From Address", description = "The email address to use in the From field", type = FieldType.TEXT)
	String fromAddress;
	
	@Column(name = "Reply-To Address", description = "The email address to use in the Reply-To field", type = FieldType.TEXT)
	String replyToAddress;

	@Column(name = "Reply-To Name", description = "The name of the user to place in the Reply-To field", type = FieldType.TEXT)
	String replyToName;
	
	@Column(name = "Archive Address", description = "An email address that should receive archived emails", type = FieldType.TEXT)
	String archiveAddress;
	
	public Boolean getEnabled() {
		return enabled;
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
