package com.jadaptive.api.repository;

import java.io.Serializable;

public interface UUIDDocument extends Serializable {

	public String getUuid();

	public void setUuid(String string);
	
	String getResourceKey();
	
	String getEventGroup();
}
