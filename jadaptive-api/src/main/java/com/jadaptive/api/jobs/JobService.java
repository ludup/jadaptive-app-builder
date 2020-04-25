package com.jadaptive.api.jobs;

public interface JobService {

	public static final String READ_WRITE_PERMISSION = "job.readWrite";
	public static final String EXECUTE_PERMISSION = "job.execute";

	void createJob(Job job);

	Job getJobByName(String name);

	Job getJob(String id);

	void scheduleExecution(Job job, String crontab);

	void runNow(Job job);

}
