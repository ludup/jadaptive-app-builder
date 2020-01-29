package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.sshd.commands.AbstractTenantAwareCommand;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Templates extends AbstractTenantAwareCommand {

	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	EntityTemplateService templateService; 
	
	public Templates() {
		super("templates", "Template Management", UsageHelper.build("templates [option]",
				"-l, --list          List all templates",
				"-c, --create        Create a new template",
				"-p                  Personal scope",
				"-a                  Assigned scope",
				"-s                  Singleton type",
				"-e                  Embedded type"),
				"Manage object templates");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} else if(args.length==1 || CliHelper.hasShortOption(args, 'l') || CliHelper.hasLongOption(args, "list")) {	
			printTemplates();
		} else if(args.length>=1 || CliHelper.hasShortOption(args, 'c') || CliHelper.hasLongOption(args, "create")) {	
			createTemplate();
		} else {
			console.println("Invalid arguments!");
			printUsage();
		}
	}

	private void createTemplate() {
		

		EntityTemplate template = new EntityTemplate();
		
		String uuid = console.readLine("Resource Key: ");
		String name = console.readLine("Name: ");
		
		template.setResourceKey(uuid);
		template.setName(name);
		template.setScope(CliHelper.hasShortOption(args, 'p') ? EntityScope.PERSONAL 
				: CliHelper.hasShortOption(args, 'a') ? EntityScope.ASSIGNED : EntityScope.GLOBAL);
		template.setType(CliHelper.hasShortOption(args, 's') ? EntityType.SINGLETON 
				: CliHelper.hasShortOption(args, 'e') ? EntityType.OBJECT : EntityType.COLLECTION);
		
		templateService.saveOrUpdate(template);
		console.println(String.format("Created user %s", template.getName()));
	}
	

	private void printTemplates() {
		for(EntityTemplate template : templateService.list()) {
			console.println(template.getName());
		}
	}
}
