package com.jadaptive.app.jobs;

import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.jobs.Job;
import com.jadaptive.api.jobs.JobService;
import com.jadaptive.api.permissions.AuthenticatedService;

@Service
public class JobServiceImpl extends AuthenticatedService implements JobService {

	@Autowired
	private TenantAwareObjectDatabase<Job> jobDatabase;

	@Override
	public void createJob(Job job) {
		
		assertWrite(Job.RESOURCE_KEY);
		
		if(Objects.isNull(job.getShortId())) {
			selectNextJobId(job);
		}
		
		jobDatabase.saveOrUpdate(job);
	}


	private void selectNextJobId(Job job) {
		
		try {
			Job j = jobDatabase.max(Job.class, "shortId");
			job.setShortId(j.getShortId() + 1);
		} catch(EntityNotFoundException e) {
			job.setShortId(100);
		}
	}


	@Override
	public Job getJobByName(String name) {
		
		assertRead(Job.RESOURCE_KEY);
		
		return jobDatabase.get(Job.class, SearchField.eq("name", name));
	}
	
	@Override
	public Job getJob(String id) {
		
		assertRead(Job.RESOURCE_KEY);
		
		if(NumberUtils.isCreatable(id)) {
			return jobDatabase.get(Job.class, SearchField.or(
					SearchField.eq("uuid", id),
					SearchField.eq("name", id),
					SearchField.eq("uuid", Integer.parseInt(id))));
		} else {
			return jobDatabase.get(Job.class, SearchField.or(
					SearchField.eq("uuid", id),
					SearchField.eq("name", id)));
		}
	}


	@Override
	public void scheduleExecution(Job job, String crontab) {
		
		
	}
}
