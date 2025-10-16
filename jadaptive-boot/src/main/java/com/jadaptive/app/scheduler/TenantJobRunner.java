package com.jadaptive.app.scheduler;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.jobs.TaskRunnerContext;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.TenantTask;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Utils;

public class TenantJobRunner implements Runnable {

	static Logger log = LoggerFactory.getLogger(TenantJobRunner.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private TaskScheduler taskScheduler;
	
	@Autowired
	private PermissionService permissionService; 
	
	String taskUUID;
	TenantTask task;
	ScheduledFuture<?> future;
	String tenantUUID;
	User user = null;
	
	public TenantJobRunner(Tenant tenant, String taskUUID) {
		this.tenantUUID = tenant.getUuid();
		this.taskUUID = taskUUID;
	}
	
	public TenantJobRunner(Tenant tenant, String taskUUID, User user) {
		this.tenantUUID = tenant.getUuid();
		this.taskUUID = taskUUID;
		this.user = user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public void schedule(ScheduledTask task) {
		this.task = task;
		future = taskScheduler.schedule(this, new CronTrigger(task.cron()));
	}
	
	public void schedule(TenantTask task, String expression) {
		this.task = task;
		future = taskScheduler.schedule(this, new CronTrigger(expression));
	}
	
	public void schedule(TenantTask task, Date startTime, long repeat) {
		this.task = task;
		future = taskScheduler.scheduleAtFixedRate(this, startTime.toInstant(), Duration.ofMillis(repeat));
	}
	
	public void runNow(TenantTask task) {
		this.task = task;
		future = taskScheduler.schedule(this, Utils.now().toInstant());
	}
	
	public void schedule(TenantTask task, Date startTime) {
		this.task = task;
		future = taskScheduler.schedule(this, startTime.toInstant());
	}
	
	public void scheduleIn(TenantTask task, Duration duration) {
		this.task = task;
		future = taskScheduler.scheduleWithFixedDelay(this, duration);
	}
	
	@Override
	public void run() {
		
		final Tenant tenant = tenantService.getTenantByUUID(tenantUUID);
		tenantService.setCurrentTenant(tenant);
		
		try {

			permissionService.as(user == null ? permissionService.getSystemUser() : user, ()->{
				for(TaskRunnerContext ctx : ApplicationServiceImpl.getInstance().getBeans(TaskRunnerContext.class)) {
					ctx.setupContext();
				}
				
				if(task.isLogging() && log.isInfoEnabled()) {
					log.info("Running {} on tenant {}", task.getClass().getSimpleName(), tenant.getName());
				}
				try {
					task.run();
				} catch(Throwable e) {
					log.error("Task ended with error", e);
			    } 
			});
		} catch(Throwable e) {
			log.error("Scheduled task {} failed", tenantUUID, e);
			future.cancel(false);
			return;
		} finally {
			tenantService.clearCurrentTenant();
		}
		
		
	}
	public void cancel(boolean mayInterrupt) {
		future.cancel(mayInterrupt);
	}

	public String getTenantUUID() {
		return tenantUUID;
	}
	
	public String getTaskUUID() {
		return taskUUID;
	}

}
