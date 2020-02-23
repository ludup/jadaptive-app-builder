package com.jadaptive.app.auth.quota;

import com.jadaptive.api.permissions.AccessDeniedException;

public interface QuotaService {

	void incrementQuota(String group, String key, long quota, long period) throws AccessDeniedException;

	void incrementQuota(String group, String key, long count, long quota, long period) throws AccessDeniedException;

}
