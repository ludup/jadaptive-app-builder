package com.jadaptive.plugins.ssh.management.commands.jobs;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.jobs.JobService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class JobCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	static Logger log = LoggerFactory.getLogger(JobCommandFactory.class);
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		
//		tryCommand("create-job", CreateJob.class, JobService.READ_WRITE_PERMISSION);
//		tryCommand("append-task", AppendTask.class, JobService.READ_WRITE_PERMISSION);
//		tryCommand("exec-job", ExecuteJob.class, JobService.EXECUTE_PERMISSION);
//		tryCommand("schedule-job", ScheduleJob.class, JobService.EXECUTE_PERMISSION);
//		tryCommand("cancel-job", CancelJob.class, JobService.READ_WRITE_PERMISSION);
		
		return this;
	}
	
}