package com.jadaptive.app.scheduler;

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
	
	public TenantJobRunner(Tenant tenant, String taskUUID) {
		this.tenantUUID = tenant.getUuid();
		this.taskUUID = taskUUID;
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
		future = taskScheduler.scheduleAtFixedRate(task, startTime, repeat);
	}
	
	public void runNow(TenantTask task) {
		this.task = task;
		future = taskScheduler.schedule(this, Utils.now());
	}
	
	@Override
	public void run() {
		
		Tenant tenant = null;
		
		try {
			tenant = tenantService.getTenantByUUID(tenantUUID);
		} catch(ObjectNotFoundException e) {
			log.error("Tenant does not exist for UUID {}", tenantUUID);
			future.cancel(false);
			return;
		}
		
		tenantService.setCurrentTenant(tenant);
		
		// TODO run as a different user
		permissionService.setupSystemContext();
		
		try {
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
		    } finally {
				tenantService.clearCurrentTenant();
			}

		} finally {
			permissionService.clearUserContext();
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
