package com.jadaptive.api.app.messaging;

import java.util.Map;

import com.jadaptive.api.user.User;

public interface MessageSubscriber {

	/**
	 * Called when a message is received from the message bus. The sourceId is the id of the source that sent the message. The user is the user that sent the message. The platform is the platform that sent the message. The metadata is a map of key-value pairs that can be used to store additional information about the message. The payload is the actual message content.
	 * @param sourceId
	 * @param user
	 * @param platform
	 * @param metadata
	 * @param payload
	 */
    void onMessage(String sourceId, User user, String platform, Map<String, Object> metadata, String payload);
}