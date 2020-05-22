package com.jadaptive.app.scheduler;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.jobs.Job;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.scheduler.CronSchedule;
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
	
	@PostConstruct
	private void postConstruct() {
		configureScheduler();
	}

	private void configureScheduler() {
		SchedulerConfiguration config = schedulerConfig.getObject(SchedulerConfiguration.class);
		scheduler.setPoolSize(config.getPoolSize());
	}

	@Override
	public void initializeSystem(boolean newSchema) {
		initializeTenant(getCurrentTenant(), newSchema);
	}

	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {
		
//		for(CronSchedule schedule : cronDatabase.list(CronSchedule.class, SearchField.not("disabled", true))) {
//			schedule(schedule.getJob(), schedule.getExpression());
//		}
	}

	@Override
	public void schedule(Job job, String expression) {
		
		CronSchedule schedule = new CronSchedule();
		schedule.setJob(job);
		schedule.setExpression(expression);
		
		cronDatabase.saveOrUpdate(schedule);
		ScheduleJobRunner runner = new ScheduleJobRunner(getCurrentTenant());
		applicationService.autowire(runner);

		runner.schedule(schedule);
		scheduledJobs.put(schedule.getUuid(), runner);
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
	public Collection<CronSchedule> getJobSchedules(Job job) {
		return cronDatabase.list(CronSchedule.class, SearchField.eq("job", job.getUuid()));
	}

	@Override
	public void runNow(Job job) {
		run(job, Instant.now());
	}
	
	@Override
	public void run(Job job, Instant startTime) {
		
		JobRunner runner = new JobRunner(job, getCurrentTenant());
		applicationService.autowire(runner);
		scheduler.schedule(runner,startTime);
		
	}
}
