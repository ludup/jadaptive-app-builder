package com.jadaptive.app.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.jadaptive.api.events.EventListener;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.SystemEvent;

@Service
public class EventServiceImpl implements EventService { 

	Collection<EventListener> eventListeners = new ArrayList<>();
	Map<String,Collection<EventListener>> keyedListeners = new HashMap<>();
	Executor eventExecutor = Executors.newCachedThreadPool();
	
	@Override
	public void publishEvent(SystemEvent<?> evt) {
	
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
	
	private void fireEvent(EventListener listener, SystemEvent<?> evt) {
		if(evt.async()) {
			eventExecutor.execute(() -> listener.onEvent(evt));
		} else {
			/**
			 * Non async events can throw exceptions back up the chain
			 */
			listener.onEvent(evt);
		}
	}

	@Override
	public void on(String resourceKey, EventListener handler) {

		if(!keyedListeners.containsKey(resourceKey)) {
			keyedListeners.put(resourceKey, new ArrayList<>());
		}
		
		keyedListeners.get(resourceKey).add(handler);
		
	}
}
