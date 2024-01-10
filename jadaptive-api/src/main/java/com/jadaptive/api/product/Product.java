package com.jadaptive.api.product;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.app.ApplicationVersion;

public interface Product extends ExtensionPoint {

	default String getName() {
		return "Jadaptive App Builder";
	}
	
	default String getVersion() {
		return Boolean.getBoolean("jadaptive.development") ? "DEV_VERSION" : ApplicationVersion.getVersion();
	}
	
	default String getPoweredBy() {
		return "Powered by Jadaptive";
	}
	
	default String getLogoResource() {
		return "/app/content/images/jadaptive-logo.png";
	}

	default String getFaviconResource() {
		return "/app/content/images/jadaptive-favicon.png";
	}

	default String getProductCode() {
		return "JADAPTIVE";
	}
	
	default String getVendor() {
		return "JADAPTIVE Limited";
	}
	
	default boolean requiresRegistration() {
		return true;
	}
	
	default boolean isTenantLicensing() {
		return false;
	}

	default boolean isRevenueGenerating() {
		return true;
	}
	
}
