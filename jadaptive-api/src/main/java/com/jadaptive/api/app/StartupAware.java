package com.jadaptive.api.app;

public interface StartupAware {

	void onApplicationStartup();
	
	Integer getStartupPosition();
}
