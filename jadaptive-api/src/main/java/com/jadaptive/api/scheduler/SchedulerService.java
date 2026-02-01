package com.jadaptive.api.scheduler;

import java.io.Serializable;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.scheduler.SchedulerTask.SchedulerTaskStatus;
import com.jadaptive.api.template.DynamicColumnService;
import com.jadaptive.api.user.User;
import com.sshtools.gardensched.ClusterID;
import com.sshtools.gardensched.IdentifiableFuture;
import com.sshtools.gardensched.ObjectStore;
import com.sshtools.gardensched.TaskErrorHandler;
import com.sshtools.gardensched.TaskSuccessHandler;

public interface SchedulerService extends AbstractUUIDObjectService<SchedulerTask>, DynamicColumnService, TaskSuccessHandler, TaskErrorHandler, ObjectStore {

	public static final String GENERIC_JAD_CLUSTER = "generic-jad-cluster";
	public static final String JAD_JGROUPS = "jad-cluster.xml";
	public static final String JAD_TCP_JGROUPS = "jad-tcp-cluster.xml";
	

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

	<V extends Serializable> IdentifiableFuture<V> future(ClusterID clusterID);

	boolean isLeader();

	SchedulerTaskStatus getStatus(ClusterID cid);

}
