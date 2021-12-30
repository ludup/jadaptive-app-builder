package com.jadaptive.app.scheduler;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.jobs.JobRunnerContext;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tasks.Task;
import com.jadaptive.api.tasks.TaskResult;
import com.jadaptive.api.tasks.TaskResultEvent;
import com.jadaptive.api.tasks.TaskService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;

public abstract class AbstractJobRunner implements Runnable {

	static Logger log = LoggerFactory.getLogger(AbstractJobRunner.class);
	
	String tenantUUID;
	
	protected AbstractJobRunner(Tenant tenant) {
		this.tenantUUID = tenant.getUuid();
	}
	
	@Autowired
	private TaskService taskService; 
	
	@Autowired
	private EventService eventService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private ApplicationService applicationService;
	
	public AbstractJobRunner() {
	}
	
	protected abstract Task getTask();
	
	@Override
	public void run() {

		setupJobContext();
		
		try {
			
			Task task = getTask();
			
			Date startedExecution = new Date();
			
			beforeJobStarts(startedExecution);
			
			TaskResult result = taskService.getTaskImplementation(task).doTask(task);
			eventService.publishEvent(new TaskResultEvent(result));

			afterJobComplete(startedExecution, new Date(), task);
			

		} catch(Throwable t) {
			log.error("Task execution failed", t);
		} finally {
			clearJobContext();
		}
	}

	protected void beforeJobStarts(Date startedExecution) { }

	protected void afterJobComplete(Date startedExecution, Date finishedExecution, Task task) { }

	protected void onClearContext() { }
	
	private void clearJobContext() {
		
		onClearContext();
		
		for(JobRunnerContext ctx : applicationService.getBeans(JobRunnerContext.class)) {
			ctx.clearContext();
		}
		
		permissionService.clearUserContext();
		tenantService.clearCurrentTenant();
		
	}

	protected void onSetupContext() { }
	
	protected void cancel() { }
	
	private void setupJobContext() {
		
		try {
			tenantService.setCurrentTenant(tenantService.getTenantByUUID(tenantUUID));
			permissionService.setupSystemContext();
			
			for(JobRunnerContext ctx : applicationService.getBeans(JobRunnerContext.class)) {
				ctx.setupContext();
			}
			
			onSetupContext();
		
		} catch(ObjectNotFoundException e) {
			log.error("Tenant not found for scheduled task");
			cancel();
		}
	}
}
