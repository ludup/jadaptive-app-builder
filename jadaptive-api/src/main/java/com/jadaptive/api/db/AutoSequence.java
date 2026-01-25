package com.jadaptive.api.db;

import com.jadaptive.api.app.J;
import com.jadaptive.api.tenant.TenantService;

public interface AutoSequence {

	static public long nextSequence(String sequenceName) {
		return J.b(DirectDatabase.class).getNextSequence("sequences", J.b(TenantService.class).getCurrentTenant().getUuid(), sequenceName);
	}
}
