package com.jadaptive.plugins.ssh.management.messages;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.plugins.email.Message;
import com.jadaptive.plugins.email.MessageService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Messages extends AbstractTenantAwareCommand {
	
	@Autowired
	private ApplicationService applicationService; 
	
	public Messages() {
		super("messages", "Message Templates", "messages", "List the message templates available on this system");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		MessageService messageService = applicationService.getBean(MessageService.class);
		
		console.println(String.format("%-20s %-30s %s", "Group", "Name", "Subject"));
		console.println(String.format("%-20s %-30s %s", "-----", "----", "-------"));
		
		for(Message message : messageService.allMessages()) {
			console.println(String.format("%-20s %-30s %s", message.getGroup(), message.getName(), message.getSubject()));
		}

	}

}
