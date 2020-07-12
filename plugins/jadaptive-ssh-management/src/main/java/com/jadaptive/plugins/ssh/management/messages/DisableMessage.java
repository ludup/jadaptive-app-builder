package com.jadaptive.plugins.ssh.management.messages;

import java.io.IOException;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.plugins.email.Message;
import com.jadaptive.plugins.email.MessageService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class DisableMessage extends AbstractTenantAwareCommand {
	
	@Autowired
	private ApplicationService applicationService; 
	
	public DisableMessage() {
		super("disable-message", "Message Templates",
					UsageHelper.build("disable-message <shortName>"), 
					"Disable a message template");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length < 3) {
			throw new UsageException("There are not enough arguments");
		}
		
		MessageService messageService = applicationService.getBean(MessageService.class);
		
		String name = args[1];
		
		Message message = messageService.getMessageByShortName(name);
		message.setEnabled(false);
		
		messageService.saveMessage(message);

	}

	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		
		switch(line.wordIndex()) {
		case 1:
			for(Message message : applicationService.getBean(MessageService.class).allMessages()) {
				candidates.add(new Candidate(message.getShortName()));
			}
			break;
		case 2:
			
		default:
			break;
		}
	}

	
}
