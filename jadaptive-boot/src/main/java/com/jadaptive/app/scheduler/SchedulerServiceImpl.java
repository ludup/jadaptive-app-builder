package com.jadaptive.app.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.scheduler.TenantTask;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;

@Service
public class SchedulerServiceImpl extends AuthenticatedService implements SchedulerService, TenantAware {

	static Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);
	
	@Autowired
	private LockableTaskScheduler scheduler;
	
	@Autowired
	private SingletonObjectDatabase<SchedulerConfiguration> schedulerConfig;
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private EventService eventService;
	
	Map<String,TenantJobRunner> scheduledJobs = new HashMap<>();
	
	private void configureScheduler() {
		SchedulerConfiguration config = schedulerConfig.getObject(SchedulerConfiguration.class);
		scheduler.setPoolSize(config.getPoolSize());
	}

	@Override
	public void initializeSystem(boolean newSchema) {
		initializeTenant(getCurrentTenant(), newSchema);
		configureScheduler();
		
		eventService.deleted(Tenant.class, (evt)-> {
			for(TenantJobRunner job : new ArrayList<>(scheduledJobs.values())) {
				if(job.getTenantUUID().equals(evt.getObject().getUuid())) {
					cancelTask(job.getTaskUUID(), true);
					scheduledJobs.remove(job.getTaskUUID());
				}
			}
		});
	}

	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {
		
		if(log.isInfoEnabled()) {
			log.info("Scheduling tasks for {}", tenant.getName());
		}
		
		for(ScheduledTask task  : applicationService.getBeans(ScheduledTask.class)) {
			if(task.isSystemOnly() && !tenant.isSystem()) {
				continue;
			}
			String scopes = System.getProperty("jadaptive.taskScopes", "NODE,GLOBAL");
			
			if(!scopes.contains(task.getScope().name())) {
				if(log.isInfoEnabled()) {
					log.info("Not scheduling task {} because it is {} scope and this node only supports {}",
							task.getClass().getSimpleName(), task.getScope().name(), scopes);
				}
				continue;
			}
			TenantJobRunner job = new TenantJobRunner(tenant, UUID.randomUUID().toString());
			applicationService.autowire(job);
			job.schedule(task);

		}
	}
	
	@Override
	public void runNow(TenantTask task) {
	
		applicationService.autowire(task);
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), UUID.randomUUID().toString());
		applicationService.autowire(job);
		
		job.runNow(task);
	}
	
	@Override
	public void schedule(TenantTask task, String expression, String taskUUID) {
		
		applicationService.autowire(task);
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), taskUUID);
		applicationService.autowire(job);

		job.schedule(task, expression);
		scheduledJobs.put(taskUUID, job);	
	}
	
	@Override
	public void schedule(TenantTask task, Date startTime, long repeat, String taskUUID) {
		
		applicationService.autowire(task);
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), taskUUID);
		applicationService.autowire(job);

		job.schedule(task, startTime, repeat);
		scheduledJobs.put(taskUUID, job);	
	}
	
	public void schedule(TenantTask task) {
		
		
	}

	@Override
	public void cancelTask(String uuid, boolean mayInterrupt) {
		
		TenantJobRunner job = scheduledJobs.get(uuid);
		if(Objects.nonNull(job)) {
			job.cancel(mayInterrupt);
		}
	}

}
