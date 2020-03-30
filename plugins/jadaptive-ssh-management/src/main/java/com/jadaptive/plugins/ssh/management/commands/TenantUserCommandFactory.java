package com.jadaptive.plugins.ssh.management.commands;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class TenantUserCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	static Logger log = LoggerFactory.getLogger(TenantUserCommandFactory.class);
	
	@Autowired
	PermissionService permissionService; 
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		
		tryCommand("roles", Roles.class, "role.read", "role.readWrite");
		tryCommand("permissions", Permissions.class, "role.read", "roles.readWrite");
		tryCommand("users", Users.class, "user.read", "user.readWrite");
		tryCommand("create-user", CreateUser.class, "user.readWrite");
		tryCommand("update-user", UpdateUser.class, "user.readWrite");
		tryCommand("delete-user", DeleteUser.class, "user.readWrite");
		
		tryCommand("templates", Templates.class, "entityTemplate.read", "entityTemplate.readWrite");
		
		tryCommand("security", Security.class, "tenant.read", "tenant.readWrite");
		
		tryCommand("import-csv", ImportCsv.class, "tenant.read", "tenant.readWrite");
		
		tryCommand("create-job", CreateJob.class, "job.readWrite");
		tryCommand("schedule-job", ScheduleJob.class, "job.readWrite");
		tryCommand("cancel-job", CancelJob.class, "job.readWrite");
		
		return this;
	}
	
}