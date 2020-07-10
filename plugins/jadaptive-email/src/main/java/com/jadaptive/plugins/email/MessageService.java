package com.jadaptive.plugins.email;

import java.util.Iterator;
import java.util.Set;

import com.jadaptive.utils.ITokenResolver;

public interface MessageService {

	Set<String> getMessageVariables(Message message);

	void sendMessage(String uuid, ITokenResolver tokenResolver, 
			Iterator<RecipientHolder> recipients,
			EmailAttachment... attachments);
	
	void sendMessage(String uuid, ITokenResolver tokenResolver, 
			String email,
			EmailAttachment... attachments);

	Iterable<Message> allMessages();

	Message getMessageByShortName(String name);

	void saveMessage(Message message);

}
