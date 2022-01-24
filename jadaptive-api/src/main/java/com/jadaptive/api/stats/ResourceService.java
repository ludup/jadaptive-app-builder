package com.jadaptive.api.stats;

public interface ResourceService {

	boolean isEnabled();
	
	String getResourceKey();
	
	long getTotalResources();
}
