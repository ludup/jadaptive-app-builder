package com.jadaptive.plugins.sshd;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = SSHDConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
@ObjectViews({@ObjectViewDefinition(value = SSHDConfiguration.GENERAL_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY),
	@ObjectViewDefinition(value = SSHDConfiguration.FILESYSTEM_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY)})
public class SSHDConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = -9112641261620391141L;

	public static final String RESOURCE_KEY = "sshdConfiguration";

	public static final String GENERAL_VIEW =  "general";
	public static final String FILESYSTEM_VIEW =  "filesystem";
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "600")
	@ObjectView(GENERAL_VIEW)
	Integer idleConnectionTimeoutSecs = 600;
	
//	@ObjectField(type = FieldType.LONG, defaultValue = "0")
//	@ObjectView(FILESYSTEM_VIEW)
//	Long homeDirectoryMaxSpace = 0L;

	@ObjectField(type = FieldType.INTEGER, defaultValue = "2097152")
	@ObjectView(FILESYSTEM_VIEW)
	Integer sftpMaximumWindowSpace = 2097152;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "1048576")
	@ObjectView(FILESYSTEM_VIEW)
	Integer sftpMinimumWindowSpace = 1048576;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "34000")
	@ObjectView(FILESYSTEM_VIEW)
	Integer sftpMaximumPacketSize = 34000;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "true")
	@ObjectView(FILESYSTEM_VIEW)
	Boolean enableSCP = true;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "UTF-8")
	@ObjectView(FILESYSTEM_VIEW)
	String sftpCharacterSetEncoding = "UTF-8";
	
//	public Long getHomeDirectoryMaxSpace() {
//		return homeDirectoryMaxSpace;
//	}
//
//	public void setHomeDirectoryMaxSpace(Long homeDirectoryMaxSpace) {
//		this.homeDirectoryMaxSpace = homeDirectoryMaxSpace;
//	}

	public Integer getIdleConnectionTimeoutSecs() {
		return idleConnectionTimeoutSecs;
	}

	public void setIdleConnectionTimeoutSecs(Integer idleConnectionTimeoutSecs) {
		this.idleConnectionTimeoutSecs = idleConnectionTimeoutSecs;
	}

	public Boolean getEnableSCP() {
		return enableSCP;
	}

	public void setEnableSCP(Boolean enableSCP) {
		this.enableSCP = enableSCP;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public Integer getSftpMaximumWindowSpace() {
		return sftpMaximumWindowSpace;
	}

	public void setSftpMaximumWindowSpace(Integer sftpMaximumWindowSpace) {
		this.sftpMaximumWindowSpace = sftpMaximumWindowSpace;
	}

	public Integer getSftpMinimumWindowSpace() {
		return sftpMinimumWindowSpace;
	}

	public void setSftpMinimumWindowSpace(Integer sftpMinimumWindowSpace) {
		this.sftpMinimumWindowSpace = sftpMinimumWindowSpace;
	}

	public Integer getSftpMaximumPacketSize() {
		return sftpMaximumPacketSize;
	}

	public void setSftpMaximumPacketSize(Integer sftpMaximumPacketSize) {
		this.sftpMaximumPacketSize = sftpMaximumPacketSize;
	}

	public String getSftpCharacterSetEncoding() {
		return sftpCharacterSetEncoding;
	}

	public void setSftpCharacterSetEncoding(String sftpCharacterSetEncoding) {
		this.sftpCharacterSetEncoding = sftpCharacterSetEncoding;
	}
}
