package com.jadaptive.app.scheduler;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.jadaptive.api.scheduler.CronSchedule;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.tasks.Task;
import com.jadaptive.api.tenant.Tenant;

public class ScheduleJobRunner extends AbstractJobRunner {

	static Logger log = LoggerFactory.getLogger(ScheduleJobRunner.class);
	
	String scheduleUUID;
	ScheduledFuture<?> future;
	
	@Autowired
	private SchedulerService schedulerService; 
	
	@Autowired
	private TaskScheduler taskExecutor;
	
	public ScheduleJobRunner(Tenant tenant) {
		super(tenant);
	}
	
	public void schedule(CronSchedule schedule) {
		this.scheduleUUID = schedule.getUuid();
		this.future = taskExecutor.schedule(this, new CronTrigger(schedule.getExpression()));
	}

	public void cancel() {
		this.future.cancel(true);
	}

	@Override
	protected void afterJobComplete(Date startedExecution, Date finishedExecution, Task task) {
		
		CronSchedule schedule = schedulerService.getSchedule(scheduleUUID);
		schedule.setLastExecutionFinished(finishedExecution);
		schedule.setLastExecutionStarted(startedExecution);
		
		schedulerService.saveSchedule(schedule);
	}
	
	@Override
	protected Task getTask() {
		CronSchedule schedule = schedulerService.getSchedule(scheduleUUID);
	
		if(schedule.getDisabled()) {
			future.cancel(false);
			throw new IllegalStateException("Schedule has been cancelled");
		}
		
		return schedule.getJob().getTask();
	}

}
