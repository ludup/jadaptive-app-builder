package com.jadaptive.api.app.messaging;

import java.util.Map;

import com.jadaptive.api.user.User;

public record Message(MessageType type, 
		String sourceId,
		String receipientId,
		User user, 
		String platform, 
		Map<String, Object> metadata, 
		String payload, 
		boolean broadcast,
		MessageSubscriber responder) {

}
