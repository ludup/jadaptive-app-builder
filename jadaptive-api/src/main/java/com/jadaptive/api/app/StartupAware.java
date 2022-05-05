package com.jadaptive.api.app;

public interface StartupAware {

	void onApplicationStartup();
	
	default Integer getStartupPosition() { return Integer.MAX_VALUE; };
}
