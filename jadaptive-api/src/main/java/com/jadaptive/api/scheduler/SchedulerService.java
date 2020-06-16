package com.jadaptive.api.scheduler;

import java.time.Instant;

import com.jadaptive.api.jobs.Job;

public interface SchedulerService {

	void schedule(Job job, String expression);

	CronSchedule getSchedule(String uuid);

	void saveSchedule(CronSchedule schedule);

	void cancelSchedule(CronSchedule schedule);

	Iterable<CronSchedule> getJobSchedules(Job job);

	void runNow(Job job);

	void run(Job job, Instant startTime);

	long getJobSchedulesCount(Job job);


}
