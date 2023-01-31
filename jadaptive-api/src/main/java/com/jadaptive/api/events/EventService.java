package com.jadaptive.api.events;

import com.jadaptive.api.repository.UUIDEntity;

public interface EventService {

	void on(String resourceKey, EventListener<?> handler);

	<T extends UUIDEntity> void created(Class<T> clz, EventListener<ObjectEvent<T>> handler);
	<T extends UUIDEntity> void updated(Class<T> clz, EventListener<ObjectEvent<T>> handler);
	<T extends UUIDEntity> void deleted(Class<T> clz, EventListener<ObjectEvent<T>> handler);
	<T extends UUIDEntity> void any(Class<T> clz, EventListener<ObjectEvent<T>> handler);	
	
	void publishEvent(SystemEvent evt);

	void registerListener(EventListener<?> listener);

	void preRegisterEventHandler(Runnable runnable);

	void executePreRegistrations();

	

}
