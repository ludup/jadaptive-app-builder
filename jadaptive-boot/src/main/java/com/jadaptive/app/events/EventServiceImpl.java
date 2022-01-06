package com.jadaptive.app.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.events.EventListener;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.Events;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.events.UUIDEntityCreatedEvent;
import com.jadaptive.api.events.UUIDEntityDeletedEvent;
import com.jadaptive.api.events.UUIDEntityUpdatedEvent;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.TemplateService;

@Service
public class EventServiceImpl implements EventService { 

	@SuppressWarnings("rawtypes")
	Collection<EventListener> eventListeners = new ArrayList<>();
	
	@SuppressWarnings("rawtypes")
	Map<String,Collection<EventListener>> keyedListeners = new HashMap<>();
	
	Executor eventExecutor = Executors.newCachedThreadPool();
	
	@Autowired
	private TemplateService templateService; 
	
	@SuppressWarnings("rawtypes")
	@Override
	public void publishEvent(SystemEvent evt) {
	
		for(EventListener listener : eventListeners) {
			fireEvent(listener, evt);
		}
		
		Collection<EventListener> keyed = keyedListeners.get(evt.getResourceKey());
		if(Objects.nonNull(keyed)) {
			for(EventListener listener : keyed) {
				fireEvent(listener, evt);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void fireEvent(EventListener listener, SystemEvent evt) {
		if(evt.async()) {
			eventExecutor.execute(() -> listener.onEvent(evt));
		} else {
			/**
			 * Non async events can throw exceptions back up the chain
			 */
			listener.onEvent(evt);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void on(String resourceKey, EventListener handler) {

		if(!keyedListeners.containsKey(resourceKey)) {
			keyedListeners.put(resourceKey, new ArrayList<>());
		}
		
		keyedListeners.get(resourceKey).add(handler);
		
	}

	@Override
	public <T extends UUIDEntity> void created(Class<T> clz, EventListener<UUIDEntityCreatedEvent<T>> handler) {
		on(Events.created(templateService.getTemplateResourceKey(clz)), handler);
	}

	@Override
	public <T extends UUIDEntity> void updated(Class<T> clz, EventListener<UUIDEntityUpdatedEvent<T>> handler) {
		on(Events.updated(templateService.getTemplateResourceKey(clz)), handler);
	}

	@Override
	public <T extends UUIDEntity> void deleted(Class<T> clz, EventListener<UUIDEntityDeletedEvent<T>> handler) {
		on(Events.deleted(templateService.getTemplateResourceKey(clz)), handler);
	}
}
