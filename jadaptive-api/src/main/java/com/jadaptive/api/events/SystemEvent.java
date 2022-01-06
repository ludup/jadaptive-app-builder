package com.jadaptive.api.events;

import java.io.Serializable;

import com.jadaptive.api.repository.JadaptiveIgnore;

public class SystemEvent implements Serializable {

	private static final long serialVersionUID = 4068966863055480029L;

	long timestamp;
	String resourceKey;
	String group;
	public SystemEvent(String resourceKey, String group) {
		this(resourceKey, group, System.currentTimeMillis());
	}
	
	public SystemEvent(String resourceKey, String group, long timestamp) {
		this.timestamp = timestamp;
		this.resourceKey = resourceKey;
		this.group = group;
	}

	public String getResourceKey() {
		return resourceKey;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getGroup() {
		return group;
	}
	
	@JadaptiveIgnore
	public boolean async() { return true; };
}
