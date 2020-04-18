package com.jadaptive.api.user;

public interface User {
	
	String getUuid();
	
	String getUsername();
	
	String getName();
	
	boolean getPasswordChangeRequired();
	
	void setPasswordChangeRequired(boolean change);
	
	String getEmail();

	void setEmail(String value);

	void setName(String value);
}
