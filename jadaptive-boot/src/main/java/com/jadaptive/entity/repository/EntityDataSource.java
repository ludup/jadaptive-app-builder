package com.jadaptive.entity.repository;

import java.util.Collection;
import java.util.Map;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.tenant.Tenant;

public interface EntityDataSource {

	String getName();
	
	void save(Tenant tenant, String resourceKey, String rootUuid, Map<String,Map<String,String>> values);
	
	Map<String,Map<String,String>> get(Tenant tenant, String resourceKey, String rootUuid) throws EntityNotFoundException;

	Collection<String> list(Tenant tenant, String resourceKeyS);

//	Collection<Map<String,String>> list(String resourceKeey);
}
