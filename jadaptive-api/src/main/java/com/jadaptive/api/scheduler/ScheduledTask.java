package com.jadaptive.api.scheduler;

import org.pf4j.ExtensionPoint;

public interface ScheduledTask extends Runnable, ExtensionPoint {

	static final String AT_MIDNIGHT =  "0 0 0 * * *";
	static final String EVERY_HOUR =   "0 0 */1 * * *";
	static final String EVERY_MINUTE = "0 */1 * * * *";
	static final String EVERY_DAY =    "0 0 0 */1 * *";
	
	String cron();

	default boolean isSystemOnly() { return false; }
}
