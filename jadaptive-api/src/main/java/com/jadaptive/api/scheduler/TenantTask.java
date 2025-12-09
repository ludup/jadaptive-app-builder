package com.jadaptive.api.scheduler;

import org.pf4j.ExtensionPoint;

import com.sshtools.gardensched.SerializableRunnable;

public interface TenantTask extends SerializableRunnable, ExtensionPoint {

	default boolean isLogging() {
		var annot = getClass().getAnnotation(TenantTaskConfig.class);
		return annot != null && annot.logging(); 
	}

}
