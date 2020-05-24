package com.jadaptive.plugins.ssh.management.commands.system;

import java.io.IOException;

import org.codemonkey.simplejavamail.MailException;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.plugins.email.EmailNotificationService;
import com.jadaptive.plugins.email.RecipientHolder;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Email extends AbstractTenantAwareCommand {

	@Autowired
	private ApplicationService applicationService;  
	
	public Email() {
		super("email", "System Management", UsageHelper.build("email"),
				"Send an email");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		String to = console.readLine("To: ");
		String subject = console.readLine("Subject: ");
		console.println("Body:");

		StringBuilder body = new StringBuilder();
		
		while(true) {
			String line = console.readLine();
			if("..".equals(line)) {
				break;
			}
			if(body.length() > 0) {
				body.append(System.lineSeparator());
			}
			body.append(line);
			
		}
		
		console.println();
		console.println("Your message is:");
		console.println("     To: " + to);
		console.println("Subject: " + subject);
		console.println(body.toString());
		
		String answer = console.readLine("Do you want to send? (y/n): ");
		
		if(answer.toLowerCase().startsWith("y")) {
			try {
				applicationService.getBean(EmailNotificationService.class).sendEmail(
						subject, body.toString(), "", 
						new RecipientHolder[] { new RecipientHolder(to)}, 
						true);
			} catch (MailException | AccessDeniedException | ValidationException e) {
				console.println("There was an error sending the email");
				console.print(e);
			}
			
		}
	}

}
