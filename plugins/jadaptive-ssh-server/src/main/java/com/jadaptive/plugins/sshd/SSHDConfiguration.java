package com.jadaptive.plugins.sshd;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = SSHDConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
public class SSHDConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = -9112641261620391141L;

	public static final String RESOURCE_KEY = "sshdConfiguration";

	@ObjectField(type = FieldType.LONG, defaultValue = "0")
	Long homeDirectoryMaxSpace = 0L;

	@ObjectField(type = FieldType.INTEGER, defaultValue = "600")
	Integer idleConnectionTimeoutSecs = 600;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "true")
	Boolean enableSCP = true;
	
	public Long getHomeDirectoryMaxSpace() {
		return homeDirectoryMaxSpace;
	}

	public void setHomeDirectoryMaxSpace(Long homeDirectoryMaxSpace) {
		this.homeDirectoryMaxSpace = homeDirectoryMaxSpace;
	}

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
	
	

}
