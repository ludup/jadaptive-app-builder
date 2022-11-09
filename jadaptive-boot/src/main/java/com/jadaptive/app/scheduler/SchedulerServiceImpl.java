package com.jadaptive.app.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.scheduler.TenantTask;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;

@Service
public class SchedulerServiceImpl extends AuthenticatedService implements SchedulerService, TenantAware {

	@Autowired
	private LockableTaskScheduler scheduler;
	
	@Autowired
	private SingletonObjectDatabase<SchedulerConfiguration> schedulerConfig;
	
	@Autowired
	private ApplicationService applicationService; 
	
	
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
		
		for(ScheduledTask task  : applicationService.getBeans(ScheduledTask.class)) {
			if(task.isSystemOnly() && !tenant.isSystem()) {
				continue;
			}
			
			TenantJobRunner job = new TenantJobRunner(tenant, UUID.randomUUID().toString());
			applicationService.autowire(job);
			job.schedule(task);

		}
	}
	
	@Override
	public void schedule(TenantTask task, String expression, String taskUUID) {
		
		
		TenantJobRunner job = new TenantJobRunner(getCurrentTenant(), taskUUID);
		applicationService.autowire(job);
		job.schedule(task, expression);
		scheduledJobs.put(taskUUID, job);	
	}

	@Override
	public void cancelTask(String uuid, boolean mayInterrupt) {
		
		TenantJobRunner job = scheduledJobs.get(uuid);
		if(Objects.nonNull(job)) {
			job.cancel(mayInterrupt);
		}
	}

}
