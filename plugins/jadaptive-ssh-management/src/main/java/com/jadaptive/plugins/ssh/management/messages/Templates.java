package com.jadaptive.plugins.ssh.management.messages;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.plugins.email.HTMLTemplate;
import com.jadaptive.plugins.email.HTMLTemplateService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Templates extends AbstractTenantAwareCommand {
	
	@Autowired
	private ApplicationService applicationService; 
	
	public Templates() {
		super("templates", "Message Templates", "templates", "List the HTML templates available on this system");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		HTMLTemplateService templateService = applicationService.getBean(HTMLTemplateService.class);
		
		console.println(String.format("%-20s %-20s %s", "Short Name", "Content Selector", "Name"));
		console.println(String.format("%-20s %-20s %s", "----------", "----------------", "----"));
		
		for(HTMLTemplate template : templateService.allTemplates()) {
			console.println(String.format("%-20s %-20s %s", template.getShortName(), template.getContentSelector(), template.getName()));
		}

	}

}
