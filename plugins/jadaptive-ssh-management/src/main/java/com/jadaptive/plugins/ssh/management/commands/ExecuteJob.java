package com.jadaptive.plugins.ssh.management.commands;

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

public class ExecuteJob extends AbstractTenantAwareCommand {
	
	@Autowired
	private JobService jobService; 
	
	@Autowired
	SchedulerService schedulerService; 
	
	public ExecuteJob() {
		super("exec-job", "Automation", 
				UsageHelper.build("exec-job <id|name>"), 
				"Execute a job");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		
		if(args.length < 2) {
			throw new UsageException("You must provide a name and template for this Job");
		}
		
		String name = args[args.length - 1];
		Job job = jobService.getJob(name);
		schedulerService.runNow(job);
		
	}

}