package com.jadaptive.plugins.ssh.management.commands;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.jobs.JobService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.ssh.management.commands.jobs.AppendTask;
import com.jadaptive.plugins.ssh.management.commands.jobs.CancelJob;
import com.jadaptive.plugins.ssh.management.commands.jobs.CreateJob;
import com.jadaptive.plugins.ssh.management.commands.jobs.ExecuteJob;
import com.jadaptive.plugins.ssh.management.commands.jobs.ScheduleJob;
import com.jadaptive.plugins.ssh.management.commands.objects.ImportCsv;
import com.jadaptive.plugins.ssh.management.commands.roles.AssignRole;
import com.jadaptive.plugins.ssh.management.commands.roles.CreateRole;
import com.jadaptive.plugins.ssh.management.commands.roles.DeleteRole;
import com.jadaptive.plugins.ssh.management.commands.roles.GrantPermission;
import com.jadaptive.plugins.ssh.management.commands.roles.Permissions;
import com.jadaptive.plugins.ssh.management.commands.roles.RevokePermission;
import com.jadaptive.plugins.ssh.management.commands.roles.Roles;
import com.jadaptive.plugins.ssh.management.commands.roles.UnassignRole;
import com.jadaptive.plugins.ssh.management.commands.users.CreateUser;
import com.jadaptive.plugins.ssh.management.commands.users.DeleteUser;
import com.jadaptive.plugins.ssh.management.commands.users.UpdateUser;
import com.jadaptive.plugins.ssh.management.commands.users.Users;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class TenantUserCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	static Logger log = LoggerFactory.getLogger(TenantUserCommandFactory.class);
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		
		tryCommand("roles", Roles.class, RoleService.READ_PERMISSION);
		tryCommand("create-role", CreateRole.class, RoleService.READ_WRITE_PERMISSION);
		tryCommand("delete-role", DeleteRole.class, RoleService.READ_WRITE_PERMISSION);
		tryCommand("assign-role", AssignRole.class, RoleService.READ_WRITE_PERMISSION);
		tryCommand("unassign-role", UnassignRole.class, RoleService.READ_WRITE_PERMISSION);
		
		tryCommand("permissions", Permissions.class, RoleService.READ_PERMISSION);
		tryCommand("grant-permission", GrantPermission.class, RoleService.READ_WRITE_PERMISSION);
		tryCommand("revoke-permission", RevokePermission.class, RoleService.READ_WRITE_PERMISSION);
		
		tryCommand("users", Users.class, UserService.READ_PERMISSION);
		tryCommand("create-user", CreateUser.class, UserService.READ_WRITE_PERMISSION);
		tryCommand("update-user", UpdateUser.class, UserService.READ_WRITE_PERMISSION);
		tryCommand("delete-user", DeleteUser.class, UserService.READ_WRITE_PERMISSION);
		
//		tryCommand("templates", Templates.class, "entityTemplate.read", "entityTemplate.readWrite");
//		tryCommand("security", Security.class, "tenant.read", "tenant.readWrite");
//		tryCommand("import-csv", ImportCsv.class, "tenant.read", "tenant.readWrite");
		
		tryCommand("create-job", CreateJob.class, JobService.READ_WRITE_PERMISSION);
		tryCommand("append-task", AppendTask.class, JobService.READ_WRITE_PERMISSION);
		tryCommand("exec-job", ExecuteJob.class, JobService.EXECUTE_PERMISSION);
		tryCommand("schedule-job", ScheduleJob.class, JobService.EXECUTE_PERMISSION);
		tryCommand("cancel-job", CancelJob.class, JobService.READ_WRITE_PERMISSION);
		
		return this;
	}
	
}