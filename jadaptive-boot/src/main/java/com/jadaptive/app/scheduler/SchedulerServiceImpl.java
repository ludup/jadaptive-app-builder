package com.jadaptive.app.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.jobs.Job;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.scheduler.CronSchedule;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;

@Service
public class SchedulerServiceImpl extends AuthenticatedService implements SchedulerService, TenantAware {

	@Autowired
	private LockableTaskScheduler scheduler;
	
	@Autowired
	private TenantAwareObjectDatabase<CronSchedule> cronDatabase;
	
	@Autowired
	private SingletonObjectDatabase<SchedulerConfiguration> schedulerConfig;
	
	@Autowired
	private ApplicationService applicationService; 
	
	
	Map<String,ScheduleJobRunner> scheduledJobs = new HashMap<>();
	
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
		
		for(CronSchedule schedule : cronDatabase.list(CronSchedule.class)) {
			schedule(schedule.getJob(), schedule.getExpression());
		}
		
		for(ScheduledTask task  : applicationService.getBeans(ScheduledTask.class)) {
			if(task.isSystemOnly() && !tenant.isSystem()) {
				continue;
			}
			
			try {
				TenantJobRunner job = new TenantJobRunner(tenant);
				applicationService.autowire(job);
				job.schedule(task);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
			
		}
	}

	@Override
	public void schedule(Job job, String expression) {
		
		CronSchedule schedule = new CronSchedule();
		schedule.setJob(job);
		schedule.setExpression(expression);
		
		cronDatabase.saveOrUpdate(schedule);
		
		try {
			ScheduleJobRunner runner = new ScheduleJobRunner(getCurrentTenant());
			applicationService.autowire(runner);
			runner.schedule(schedule);
			scheduledJobs.put(schedule.getUuid(), runner);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		
	}

	@Override
	public void cancelSchedule(CronSchedule schedule) {
		ScheduleJobRunner runner = scheduledJobs.get(schedule.getUuid());
		if(Objects.nonNull(runner)) {
			runner.cancel();
		}
	}
	@Override
	public CronSchedule getSchedule(String uuid) {
		return cronDatabase.get(uuid, CronSchedule.class);
	}

	@Override
	public void saveSchedule(CronSchedule schedule) {
		cronDatabase.saveOrUpdate(schedule);
	}

	@Override
	public Iterable<CronSchedule> getJobSchedules(Job job) {
		return cronDatabase.list(CronSchedule.class, SearchField.eq("job", job.getUuid()));
	}
	
	@Override
	public long getJobSchedulesCount(Job job) {
		return cronDatabase.count(CronSchedule.class, SearchField.eq("job", job.getUuid()));
	}

	@Override
	public void runNow(Job job) {
		run(job, Instant.now());
	}
	
	@Override
	public void run(Job job, Instant startTime) {
		
		try {
			JobRunner runner = new JobRunner(job, getCurrentTenant());
			applicationService.autowire(runner);
			scheduler.schedule(runner,startTime);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
	}

}
