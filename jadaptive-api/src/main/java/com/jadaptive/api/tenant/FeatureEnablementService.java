package com.jadaptive.api.tenant;

import java.util.Set;

import com.jadaptive.api.permissions.FeatureGroup;

public interface FeatureEnablementService {

	boolean isEnabled(String resourceKey) throws TenantRequiredException;

	boolean isFeature(String resourceKey);

	Set<String> getEnabledFeatures();

	Set<String> allFeatures();

	void assertFeature(String feature);

	Set<String> getFeaturesByGroup(FeatureGroup group);
	
}
