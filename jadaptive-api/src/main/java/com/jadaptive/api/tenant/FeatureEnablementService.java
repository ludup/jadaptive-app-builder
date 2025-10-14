package com.jadaptive.api.tenant;

import java.util.Set;

import com.jadaptive.api.permissions.FeatureGroup;
import com.jadaptive.api.permissions.LicensedFeature;

public interface FeatureEnablementService {

	boolean isEnabled(String resourceKey) throws TenantRequiredException;

	boolean isFeature(String resourceKey);

	Set<LicensedFeature> getEnabledFeatures();

	Set<LicensedFeature> allFeatures();

	void assertFeature(String feature);

	Set<LicensedFeature> getFeaturesByGroup(FeatureGroup group);

	FeatureGroup edition();
	
	boolean isEvaluation();
	
}
