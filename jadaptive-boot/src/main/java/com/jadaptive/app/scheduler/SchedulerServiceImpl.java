package com.jadaptive.app.scheduler;

import java.time.Duration;
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

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.scheduler.TaskScope;
import com.jadaptive.api.scheduler.TenantTask;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.user.User;

@Service
public class SchedulerServiceImpl extends AuthenticatedService implements SchedulerService, TenantAware, StartupAware {

	static Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);
	
	@Autowired
	private LockableTaskScheduler scheduler;
	
	@Autowired
	private SingletonObjectDatabase<SchedulerConfiguration> schedulerConfig;
	
	@Autowired
	private App applicationService; 
	
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
			String scopes = ApplicationProperties.getValue("ha.taskScopes", "NODE,GLOBAL");
			
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
	public void runNow(Runnable task) {
	
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), UUID.randomUUID().toString());
		applicationService.autowire(job);
		job.runNow(new TenantTask() {
			
			@Override
			public void run() {
				task.run();
			}
			
			@Override
			public TaskScope getScope() {
				return TaskScope.NODE;
			}
		});

	}
	
	@Override
	public void runAs(User user, Runnable task) {
	
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), UUID.randomUUID().toString());
		applicationService.autowire(job);
		job.runNow(new TenantTask() {
	
			@Override
			public void run() {
				permissionService.as(user, ()->{
					task.run();
				});
			}
			
			@Override
			public TaskScope getScope() {
				return TaskScope.NODE;
			}
		});

	}
	
	@Override
	public void scheduleIn(Runnable task, Duration duration, User user) {
	
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), UUID.randomUUID().toString(), user);
		applicationService.autowire(job);
		scheduleIn(new TenantTask() {
	
			@Override
			public void run() {
				task.run();
			}
			
			@Override
			public TaskScope getScope() {
				return TaskScope.NODE;
			}
		}, duration);

	}
	
	private void scheduleIn(TenantTask task, Duration duration) {
		
		String taskUUID = UUID.randomUUID().toString();
		applicationService.autowire(task);
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), taskUUID);
		applicationService.autowire(job);

		job.scheduleIn(task, duration);
		scheduledJobs.put(taskUUID, job);	

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
	
	@Override
	public void schedule(TenantTask task, Date startTime, String taskUUID) {
		
		applicationService.autowire(task);
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), taskUUID);
		applicationService.autowire(job);

		job.schedule(task, startTime);
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

	@Override
	public void onApplicationStartup() {
		
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
	public void scheduleIn(Runnable runnable, Duration duration) {
		scheduleIn(runnable, duration, null);
	}

}
