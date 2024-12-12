package com.jadaptive.api.app;

public interface PropertyService {

	boolean getBoolean(String key, boolean defaultValue);

	void setBoolean(String key, boolean value);

	int getInteger(String key, int defaultValue);

	void setInteger(String key, int value);

	String getString(String key, String defaultValue);

	void setString(String key, String value);

	String getStringOrSaveDefault(String key, String defaultValue);

	boolean getSystemBoolean(String key, boolean defaultValue);

	void setSystemBoolean(String key, boolean value);

	int getSystemInteger(String key, int defaultValue);

	void setSystemInteger(String key, int value);

	String getSystemString(String key, String defaultValue);

	String getSystemStringOrSaveDefault(String key, String defaultValue);

	void setSystemString(String key, String value);

}
