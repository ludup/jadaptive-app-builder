package com.jadaptive.app.scheduler;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.jobs.TaskRunnerContext;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.scheduler.CronSchedule;
import com.jadaptive.api.scheduler.TenantTask;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;

public class TaskJobRunner implements Runnable {

	static Logger log = LoggerFactory.getLogger(TaskJobRunner.class);
	
	String tenantUUID;
	String scheduleUUID;
	TenantTask task;
	ScheduledFuture<?> future;
	
	@Autowired
	private TaskScheduler taskExecutor;
	
	protected TaskJobRunner(Tenant tenant, TenantTask task) {
		this.tenantUUID = tenant.getUuid();
		this.task = task;
	}
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private ApplicationService applicationService;
	
	public TaskJobRunner() {
	}
	
	protected TenantTask getTask() { return task; }
	
	@Override
	public void run() {

		setupContext();
		
		try {
			
			TenantTask task = getTask();
			
			Date startedExecution = new Date();
			
			beforeJobStarts(startedExecution);
			
			task.run();

			afterJobComplete(startedExecution, new Date(), task);
			

		} catch(Throwable t) {
			log.error("Task execution failed", t);
		} finally {
			clearContext();
		}
	}
	
	public void schedule(CronSchedule schedule) {
		this.scheduleUUID = schedule.getUuid();
		this.future = taskExecutor.schedule(this, new CronTrigger(schedule.getExpression()));
	}


	protected void beforeJobStarts(Date startedExecution) { }

	protected void afterJobComplete(Date startedExecution, Date finishedExecution, TenantTask task) { }

	protected void onClearContext() { }
	
	private void clearContext() {
		
		onClearContext();
		
		for(TaskRunnerContext ctx : applicationService.getBeans(TaskRunnerContext.class)) {
			ctx.clearContext();
		}
		
		permissionService.clearUserContext();
		tenantService.clearCurrentTenant();
		
	}

	protected void onSetupContext() { }
	
	protected void cancel() {
		future.cancel(false);
	}
	
	private void setupContext() {
		
		try {
			tenantService.setCurrentTenant(tenantService.getTenantByUUID(tenantUUID));
			permissionService.setupSystemContext();
			
			for(TaskRunnerContext ctx : applicationService.getBeans(TaskRunnerContext.class)) {
				ctx.setupContext();
			}
			
			onSetupContext();
		
		} catch(ObjectNotFoundException e) {
			log.error("Tenant not found for scheduled task");
			cancel();
		}
	}
}
