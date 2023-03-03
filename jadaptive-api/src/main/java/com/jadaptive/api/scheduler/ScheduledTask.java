package com.jadaptive.api.scheduler;

public interface ScheduledTask extends TenantTask {

	static final String AT_MIDNIGHT =  "0 0 0 * * *";
	static final String EVERY_HOUR =   "0 0 */1 * * *";
	static final String EVERY_MINUTE = "0 */1 * * * *";
	static final String EVERY_5_MINUTES = "0 */5 * * * *";
	static final String EVERY_10_MINUTES = "0 */10 * * * *";
	static final String EVERY_DAY =    "0 0 0 */1 * *";
	
	String cron();
	
	default boolean isSystemOnly() { return false; }
}
