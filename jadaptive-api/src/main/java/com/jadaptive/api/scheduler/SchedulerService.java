package com.jadaptive.api.scheduler;

public interface SchedulerService {

//	String schedule(Job job, String expression);
	
	void schedule(TenantTask job, String expression, String taskUuid);

	void cancelTask(String uuid, boolean mayInterrupt);

	void runNow(TenantTask task);

//	CronSchedule getSchedule(String uuid);
//
//	void saveSchedule(CronSchedule schedule);
//
//	void cancelSchedule(CronSchedule schedule);

//	Iterable<CronSchedule> getJobSchedules(Job job);

//	void runNow(Job job);

//	void run(Job job, Instant startTime);

//	long getJobSchedulesCount(Job job);

}
