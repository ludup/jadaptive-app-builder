package com.jadaptive.plugins.ssh.management.commands.objects;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Templates extends AbstractTenantAwareCommand {

	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	TemplateService templateService; 
	
	public Templates() {
		super("templates", "Object Management", UsageHelper.build("templates [option]",
				"-l, --list          List all templates",
				"-c, --create        Create a new template",
				"-p                  Personal scope",
				"-a                  Assigned scope",
				"-s                  Singleton type",
				"-e                  Embedded type"),
				"Manage object templates");
	}

	public boolean isHidden() {
		return true;
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
		

		ObjectTemplate template = new ObjectTemplate();
		
		String uuid = console.readLine("Resource Key: ");
		String name = console.readLine("Name: ");
		
		template.setResourceKey(uuid);
		template.setName(name);
		template.setScope(CliHelper.hasShortOption(args, 'p') ? ObjectScope.PERSONAL 
				: CliHelper.hasShortOption(args, 'a') ? ObjectScope.ASSIGNED : ObjectScope.GLOBAL);
		template.setType(CliHelper.hasShortOption(args, 's') ? ObjectType.SINGLETON 
				: CliHelper.hasShortOption(args, 'e') ? ObjectType.OBJECT : ObjectType.COLLECTION);
		
		templateService.saveOrUpdate(template);
		console.println(String.format("Created template %s", template.getName()));
	}
	

	private void printTemplates() {
		for(ObjectTemplate template : templateService.list()) {
			console.println(template.getName());
		}
	}
}
