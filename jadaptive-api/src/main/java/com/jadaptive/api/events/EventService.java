package com.jadaptive.api.events;

import com.jadaptive.api.repository.UUIDEntity;

public interface EventService {

	void on(String resourceKey, EventListener<?> handler);

	<T extends UUIDEntity> void created(Class<T> clz, EventListener<ObjectEvent<T>> handler);
	<T extends UUIDEntity> void updated(Class<T> clz, EventListener<ObjectUpdateEvent<T>> handler);
	<T extends UUIDEntity> void deleted(Class<T> clz, EventListener<ObjectEvent<T>> handler);
	<T extends UUIDEntity> void any(Class<T> clz, EventListener<ObjectEvent<T>> handler);	
	<T extends UUIDEntity> void assigned(Class<T> clz, EventListener<ObjectUpdateEvent<T>> handler);
	<T extends UUIDEntity> void unassigned(Class<T> clz, EventListener<ObjectUpdateEvent<T>> handler);
	
	void publishEvent(SystemEvent evt);

	void registerListener(EventListener<?> listener);

	void eventRegistrations(Runnable runnable);

	void executePreRegistrations();

	<T extends UUIDEntity> void creating(Class<T> clz, EventListener<ObjectEvent<T>> handler);

	<T extends UUIDEntity> void updating(Class<T> clz, EventListener<ObjectUpdateEvent<T>> handler);

	<T extends UUIDEntity> void deleting(Class<T> clz, EventListener<ObjectEvent<T>> handler);

	<T extends UUIDEntity> void on(EventListener<ObjectEvent<T>> handler, String... resourceKeys);

	<T extends UUIDEntity> void changed(Class<T> clz, EventListener<ObjectEvent<T>> handler);

	void haltEvents();

	void resumeEvents();

	<T extends UUIDEntity> void on(String eventKey, Class<T> clz, EventListener<ObjectEvent<T>> handler);
	
	<T extends UUIDEntity, E extends ObjectEvent<T>> void on(String eventKey, Class<E> eventClz, Class<T> clz, EventListener<E> handler);

	<T extends UUIDEntity> void saved(Class<T> clz, EventListener<ObjectEvent<T>> handler);

	<T extends UUIDEntity> void saving(Class<T> clz, EventListener<ObjectEvent<T>> handler);

	<T extends SystemEvent> void on(Class<T> clz, EventListener<T> handler);

	

}
