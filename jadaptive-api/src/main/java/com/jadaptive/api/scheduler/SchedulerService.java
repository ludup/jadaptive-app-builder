package com.jadaptive.api.scheduler;

import java.util.Date;

import com.jadaptive.api.user.User;

public interface SchedulerService {

	void schedule(TenantTask job, String expression, String taskUuid);

	void cancelTask(String uuid, boolean mayInterrupt);

	void runNow(TenantTask task);

	void schedule(TenantTask task, Date startTime, long repeat, String taskUUID);

	void schedule(TenantTask task, Date startTime, String taskUUID);

	void runNow(Runnable task);

	void runAs(User currentUser, Runnable task);

}
