package com.jadaptive.api.app;

public interface StartupAware {
	
	default void onAfterApplicationStartup() {}

	void onApplicationStartup();
	
	default Integer getStartupPosition() { return Integer.MAX_VALUE; };
}
