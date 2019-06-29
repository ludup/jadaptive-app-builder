package com.jadaptive.templates;

import java.util.Date;
import java.util.Objects;

import com.jadaptive.repository.ReflectionUUIDEntity;

public class Template extends ReflectionUUIDEntity {

	String version; 
	Date timestamp;
	
	public Date getTimestamp() {
		return Objects.isNull(timestamp) ? new Date() : timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getVersion() {
		return version.replace(".json", "");
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
