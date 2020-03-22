package com.jadaptive.app.scheculer;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.scheculer.ScheduledJob;
import com.jadaptive.api.scheculer.SchedulerService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;

@Service
public class SchedulerServiceImpl extends AuthenticatedService implements SchedulerService, TenantAware, TaskScheduler {

	@Autowired
	private LockableTaskScheduler scheduler;
	
	@Autowired
	private TenantAwareObjectDatabase<ScheduledJob> jobDatabase;
	
	@Autowired
	private SingletonObjectDatabase<SchedulerConfiguration> schedulerConfig;
	
	@PostConstruct
	private void postConstruct() {
		configureScheduler();
	}

	private void configureScheduler() {
		SchedulerConfiguration config = schedulerConfig.getObject(SchedulerConfiguration.class);
		scheduler.setPoolSize(config.getPoolSize());
	}

	@Override
	public void initializeSystem() {
		initializeTenant(getCurrentTenant());
	}

	@Override
	public void initializeTenant(Tenant tenant) {
		
		for(ScheduledJob job : jobDatabase.list(ScheduledJob.class)) {
			
			
		}
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		// TODO Auto-generated method stub
		return null;
	}
}
