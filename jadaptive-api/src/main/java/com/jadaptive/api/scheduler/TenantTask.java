package com.jadaptive.api.scheduler;

import org.pf4j.ExtensionPoint;

import com.sshtools.gardensched.SerializableJob;

public interface TenantTask extends SerializableJob, ExtensionPoint {

	default boolean isLogging() {
		var annot = getClass().getAnnotation(TenantTaskConfig.class);
		return annot != null && annot.logging(); 
	}

}
