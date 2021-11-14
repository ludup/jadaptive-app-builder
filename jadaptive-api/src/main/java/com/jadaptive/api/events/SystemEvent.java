package com.jadaptive.api.events;

import java.io.Serializable;

import com.jadaptive.api.repository.JadaptiveIgnore;

public class SystemEvent<T> implements Serializable {

	private static final long serialVersionUID = 4068966863055480029L;

	long timestamp;
	String resourceKey;
	String group;
	public SystemEvent(String resourceKey, String group) {
		this(resourceKey, System.currentTimeMillis());
	}
	
	public SystemEvent(String resourceKey, long timestamp) {
		this.timestamp = timestamp;
		this.resourceKey = resourceKey;
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
