package com.jadaptive.api.db;

import com.jadaptive.api.repository.AssignableUUIDEntity;

public interface AssignableObjectDatabase<T extends AssignableUUIDEntity> 
			extends TenantAwareObjectDatabase<T> {

}
