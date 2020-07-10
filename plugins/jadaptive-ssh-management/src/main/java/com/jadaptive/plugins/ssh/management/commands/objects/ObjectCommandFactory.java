package com.jadaptive.plugins.ssh.management.commands.objects;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class ObjectCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	static Logger log = LoggerFactory.getLogger(ObjectCommandFactory.class);
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		
//		tryCommand("templates", Templates.class, "objectTemplates.read", "objectTemplates.readWrite");
//		tryCommand("import-csv", ImportCsv.class, "tenant.read", "tenant.readWrite");
		tryCommand("set-config", SetConfig.class, "tenant.read", "tenant.readWrite");
		tryCommand("show-config", ShowConfig.class, "tenant.read", "tenant.readWrite");
		tryCommand("create-object", CreateObject.class, "tenant.read", "tenant.readWrite");
		
		return this;
	}
	
}