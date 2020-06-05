package com.jadaptive.api.app;

public interface PropertyService {

	boolean getBoolean(String key, boolean defaultValue);

	void setBoolean(String key, boolean value);

	int getInteger(String key, int defaultValue);

	void setInteger(String key, int value);

	String getString(String key, String defaultValue);

	void setString(String key, String value);

}
