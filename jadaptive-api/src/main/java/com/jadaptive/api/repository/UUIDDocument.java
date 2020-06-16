package com.jadaptive.api.repository;

import java.io.Serializable;

public interface UUIDDocument extends Serializable {

	public String getUuid();
	
	public Boolean isSystem();
	
	public Boolean isHidden();

	public void setUuid(String string);
}
