package com.jadaptive.api.scheduler;

public interface SchedulerService {

	void schedule(TenantTask job, String expression, String taskUuid);

	void cancelTask(String uuid, boolean mayInterrupt);

	void runNow(TenantTask task);

}
