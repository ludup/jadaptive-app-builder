package com.jadaptive.api.jobs;

public interface JobService {

	void createJob(Job job);

	Job getJobByName(String name);

	Job getJob(String id);

	void scheduleExecution(Job job, String crontab);

}
