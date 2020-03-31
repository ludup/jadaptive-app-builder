package com.jadaptive.app.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.jobs.Job;
import com.jadaptive.api.jobs.JobService;
import com.jadaptive.api.tasks.Task;
import com.jadaptive.api.tenant.Tenant;

public class JobRunner extends AbstractJobRunner {

	String jobUUID;

	public JobRunner(Job job, Tenant tenant) {
		super(tenant);
		this.jobUUID = job.getUuid();
	}
	
	@Autowired
	private JobService jobService; 
	
	@Override
	protected Task getTask() {
		return jobService.getJob(jobUUID).getTask();
	}
}
