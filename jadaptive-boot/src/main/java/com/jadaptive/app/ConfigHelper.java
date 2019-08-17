package com.jadaptive.app;

import java.io.File;

import com.jadaptive.tenant.Tenant;

public class ConfigHelper {


	public static File getConfFolder() {
		return new File(System.getProperty("jadaptive.templatePath", "conf"));
	}

	public static File getSystemFolder() {
		return new File(getConfFolder(), "system");
	}
	
	public static File getSystemSubFolder(String folder) {
		return new File(getSystemFolder(), folder);
	}

	public static File getSharedFolder() {
		return new File(getSystemFolder(), "shared");
	}
	
	public static File getSharedSubFolder(String folder) {
		return new File(getSharedFolder(), folder);
	}
	
	public static File getSystemPrivateFolder() {
		return new File(getSystemFolder(), "private");
	}
	
	public static File getSystemPrivateSubFolder(String folder) {
		return new File(getSystemPrivateFolder(), folder);
	}
	
	public static File getTenantsFolder() {
		return new File(getConfFolder(), "tenants");
	}
	
	public static File getTenantFolder(Tenant tenant) {
		return new File(getTenantsFolder(), tenant.getHostname());
	}
	
	public static File getTenantSubFolder(Tenant tenant, String folder) {
		return new File(getTenantFolder(tenant), folder);
	}
	
}
