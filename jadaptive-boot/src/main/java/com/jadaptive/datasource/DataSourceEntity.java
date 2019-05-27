package com.jadaptive.datasource;

import java.text.ParseException;
import java.util.Map;

public interface DataSourceEntity {

	void store(Map<String, Map<String, String>> properties) throws ParseException;

	void load(String uuid, Map<String, Map<String, String>> properties) throws ParseException;

}
