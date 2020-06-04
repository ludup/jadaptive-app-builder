package com.jadaptive.api.user;

import java.io.Serializable;

public interface User extends Serializable {
	
	String getUuid();
	
	String getUsername();
	
	String getName();

	void setName(String value);
}
