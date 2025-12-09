package com.jadaptive.api.scheduler;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.template.DynamicColumnService;
import com.jadaptive.api.user.User;

public interface SchedulerService extends AbstractUUIDObjectService<SchedulerTask>, DynamicColumnService {

	void schedule(TenantTask job, String expression, String taskUuid);

	void cancelTask(String uuid, boolean mayInterrupt);

	void runNow(TenantTask task);

	void schedule(TenantTask task, Date startTime, long repeat, String taskUUID);

	void schedule(TenantTask task, Date startTime, String taskUUID);

	void runNow(Runnable task);

	void runAs(User currentUser, Runnable task);

	void scheduleIn(Runnable task, Duration duration, User user);

	void scheduleIn(Runnable runnable, Duration ofMinutes);

	ScheduledExecutorService getExecutor();

	void runScheduledTaskNow(String uuid);

}
