package com.jadaptive.api.repository;

public interface UUIDDocument {

	public String getUuid();
	
	public Boolean isSystem();
	
	public Boolean isHidden();

	public void setUuid(String string);
}
