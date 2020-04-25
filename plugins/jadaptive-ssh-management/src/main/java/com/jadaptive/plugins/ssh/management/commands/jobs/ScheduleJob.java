package com.jadaptive.plugins.ssh.management.commands.jobs;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.jobs.Job;
import com.jadaptive.api.jobs.JobService;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class ScheduleJob extends AbstractTenantAwareCommand {
	
	@Autowired
	private JobService jobService; 
	
	@Autowired
	SchedulerService schedulerService; 
	
	public ScheduleJob() {
		super("schedule-job", "Automation", 
				UsageHelper.build("schedule-job <id|name> <schedule>"), 
				"Schedule a job");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		
		if(args.length < 3) {
			throw new UsageException("You must provide a name and template for this Job");
		}
		
		String name = args[1];
		StringBuffer crontab = new StringBuffer();
		for(int i = 2;i< args.length; i++) {
			if(crontab.length() > 0) {
				crontab.append(" ");
			}
			crontab.append(args[i]);
		}

		
		Job job = jobService.getJob(name);
		jobService.scheduleExecution(job, crontab.toString());
		
	}

}
