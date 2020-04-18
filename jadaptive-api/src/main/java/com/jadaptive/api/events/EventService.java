package com.jadaptive.api.events;

import com.jadaptive.api.repository.UUIDEntity;

public interface EventService {

	<T extends UUIDEntity> void publishStandardEvent(EventType type, T evt);
	
	<T extends CustomEvent> void publishCustomEvent(T evt);

}
