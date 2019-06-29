package com.jadaptive.datasource;

import java.util.Collection;
import java.util.Map;

import com.jadaptive.entity.EntityNotFoundException;

public interface EntityDataSource {

	String getName();
	
	void save(String tenantUuid, String resourceKey, String rootUuid, Map<String,Map<String,String>> values);
	
	Map<String,Map<String,String>> get(String tenantUuid, String resourceKey, String rootUuid) throws EntityNotFoundException;

	Collection<String> list(String tenantUuid, String resourceKey);
}
