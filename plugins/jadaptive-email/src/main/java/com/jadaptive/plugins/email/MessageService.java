package com.jadaptive.plugins.email;

import java.util.Set;

import com.jadaptive.utils.ITokenResolver;

public interface MessageService {

	Set<String> getMessageVariables(Message message);

	void sendMessage(String uuid, ITokenResolver tokenResolver, 
			Iterable<RecipientHolder> recipients,
			EmailAttachment... attachments);
	
	void sendMessage(String uuid, ITokenResolver tokenResolver, 
			String email,
			EmailAttachment... attachments);

	Iterable<Message> allMessages();
	
	void saveMessage(Message message);

	Message getMessageByUUID(String uuid);

}
