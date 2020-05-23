package com.jadaptive.plugins.ssh.management.commands.users;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class UserManagementCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	static Logger log = LoggerFactory.getLogger(UserManagementCommandFactory.class);
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		
		
		tryCommand("users", Users.class, UserService.READ_PERMISSION);
		tryCommand("create-user", CreateUser.class, UserService.READ_WRITE_PERMISSION);
		tryCommand("update-user", UpdateUser.class, UserService.READ_WRITE_PERMISSION);
		tryCommand("delete-user", DeleteUser.class, UserService.READ_WRITE_PERMISSION);
		
		return this;
	}
	
}