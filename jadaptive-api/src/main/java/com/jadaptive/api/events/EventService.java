package com.jadaptive.api.events;


public interface EventService {

	void on(String resourceKey, EventListener handler);

	void publishEvent(SystemEvent<?> evt);

}
