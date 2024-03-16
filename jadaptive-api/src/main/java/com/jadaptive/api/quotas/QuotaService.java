package com.jadaptive.api.quotas;

import com.jadaptive.api.permissions.AccessDeniedException;

public interface QuotaService {

	void incrementQuota(QuotaThreshold quota, String bundle, String key) throws AccessDeniedException;

	void incrementQuota(QuotaThreshold quota, long count, String bundle, String key) throws AccessDeniedException;

	void registerKey(String uuid, String name);

	boolean hasKey(String uuid);

	QuotaKey getKey(String uuid);

	QuotaThreshold getAssignedThreshold(QuotaKey key);

	void createQuota(IPQuota quota);

	void assertQuota(QuotaThreshold quota, String bundle, String key);

	long getUsedQuota(QuotaThreshold transferQuota);

	long getRemainingQuota(QuotaThreshold quota);
}
