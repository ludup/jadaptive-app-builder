package com.jadaptive.plugins.ssh.management.messages;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionUtils;
import com.jadaptive.plugins.email.HTMLTemplate;
import com.jadaptive.plugins.email.Message;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class MessageCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	static Logger log = LoggerFactory.getLogger(MessageCommandFactory.class);

	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		
		tryCommand("messages", Messages.class, PermissionUtils.getReadPermission(Message.RESOURCE_KEY));
		tryCommand("templates", Templates.class, PermissionUtils.getReadPermission(HTMLTemplate.RESOURCE_KEY));

		tryCommand("change-message-subject", ChangeMessageSubject.class, 
				PermissionUtils.getReadWritePermission(Message.RESOURCE_KEY));
		tryCommand("change-message-text", ChangeMessageText.class, 
				PermissionUtils.getReadWritePermission(Message.RESOURCE_KEY));
		tryCommand("change-message-html", ChangeMessageHtml.class, 
				PermissionUtils.getReadWritePermission(Message.RESOURCE_KEY));
		
		tryCommand("create-html-template", CreateHtmlTemplate.class, 
				PermissionUtils.getReadWritePermission(HTMLTemplate.RESOURCE_KEY));
		tryCommand("change-template-html", ChangeHtmlTemplate.class, 
				PermissionUtils.getReadWritePermission(HTMLTemplate.RESOURCE_KEY));
		
		return this;
	}
	

}
