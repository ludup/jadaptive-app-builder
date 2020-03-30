package com.jadaptive.app.scheduler;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.events.AuditService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.scheduler.CronSchedule;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.tasks.Task;
import com.jadaptive.api.tasks.TaskResult;
import com.jadaptive.api.tasks.TaskService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;

public class JobRunner implements Runnable {

	static Logger log = LoggerFactory.getLogger(JobRunner.class);
	
	String tenantUUID;
	String scheduleUUID;
	ScheduledFuture<?> future;
	
	@Autowired
	private SchedulerService schedulerService; 
	
	@Autowired
	private TaskScheduler taskExecutor;
	
	@Autowired
	private TaskService taskService; 
	
	@Autowired
	private AuditService eventService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PermissionService permissionService;
	
	public JobRunner() {
	}
	
	@Override
	public void run() {

		// TODO setup tenant and system context
		// TODO allow extensions to add to context
		setupJobContext();
		
		try {
			CronSchedule schedule = schedulerService.getSchedule(scheduleUUID);
		
			if(schedule.getDisabled()) {
				future.cancel(false);
				return;
			}
		
			Task task = schedule.getJob().getTask();
			
			Date startedExecution = new Date();
			
			TaskResult result = taskService.createTaskImpl(task).doTask(task);
			
			if(Objects.nonNull(result)) {
				eventService.publishEvent(result);
			}
			
			updateTimestamps(startedExecution, schedule);
			
		} catch(EntityNotFoundException e) {
			log.error("A schedule with uuid {} no longer exists", scheduleUUID);
			future.cancel(false);
		} catch(Throwable t) {
			log.error("Task execution failed", t);
		} finally {
			clearJobContext();
		}
	}

	private void clearJobContext() {
		
		permissionService.clearUserContext();
		tenantService.clearCurrentTenant();
		
	}

	private void setupJobContext() {
		
		tenantService.setCurrentTenant(tenantService.getTenantByUUID(tenantUUID));
		permissionService.setupSystemContext();
	}

	private void updateTimestamps(Date started, CronSchedule schedule) {
		schedule.setLastExecutionStarted(started);
		schedule.setLastExecutionFinished(new Date());
		
		schedulerService.saveSchedule(schedule);
		
	}

	public void schedule(Tenant tenant, CronSchedule schedule) {
		this.tenantUUID = tenant.getUuid();
		this.scheduleUUID = schedule.getUuid();
		this.future = taskExecutor.schedule(this, new CronTrigger(schedule.getExpression()));
	}

	public void cancel() {
		this.future.cancel(true);
	}

}
