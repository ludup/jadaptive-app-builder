package com.jadaptive.app.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.events.EventListener;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.Events;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.events.ObjectUpdateEvent;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;

@Service
public class EventServiceImpl implements EventService { 

	static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
	
	List<EventListener<?>> eventListeners = new ArrayList<>();
	
	@SuppressWarnings("rawtypes")
	Map<String,Collection<EventListener>> keyedListeners = new HashMap<>();
	
	Executor eventExecutor = Executors.newCachedThreadPool();
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private TenantService tenantService; 
	
	List<Runnable> preRegistrations = new ArrayList<>();
	
	ThreadLocal<Boolean> disableThreadEvents = new ThreadLocal<>();
	ThreadLocal<Collection<User>> assignments = new ThreadLocal<>();
	ThreadLocal<Collection<User>> unassignments = new ThreadLocal<>();
	
	@SuppressWarnings("rawtypes")
	@Override
	public void publishEvent(SystemEvent evt) {
	
		if(!isFiring()) {
			return;
		}
		
		if(evt instanceof ObjectEvent) {
			ObjectEvent<?> oevt = (ObjectEvent<?>)evt;
			if(oevt.getObject() instanceof NamedDocument) {
				oevt.setEventDescription(((NamedDocument)oevt.getObject()).getName());
			}
		}
		for(EventListener listener : eventListeners) {
			fireEvent(listener, evt);
		}
		
		Collection<EventListener> keyed = keyedListeners.get(evt.getEventGroup());
		if(Objects.nonNull(keyed)) {
			for(EventListener listener : keyed) {
				fireEvent(listener, evt);
			}
		}
		
		keyed = keyedListeners.get(evt.getEventKey());
		if(Objects.nonNull(keyed)) {
			for(EventListener listener : keyed) {
				fireEvent(listener, evt);
			}
		}

	}
	
	private boolean isFiring() {
		return disableThreadEvents.get() == null
				&& disableThreadEvents.get() != Boolean.TRUE;
	}
	
	@Override
	public void eventRegistrations(Runnable runnable) {
		if(Objects.isNull(preRegistrations)) {
			preRegistrations = new ArrayList<>();
		}
		preRegistrations.add(runnable);
	}
	
	@Override 
	public void executePreRegistrations() {
		for(Runnable r : preRegistrations) {
			try {
				r.run();
			} catch(Throwable e) {
				log.error("Error in event pre-registration handler", e);
			}
		}
		preRegistrations.clear();
		preRegistrations = null;
	}

	@Override
	public void registerListener(EventListener<?> listener) {
		eventListeners.add(listener);
		Collections.sort(eventListeners, new Comparator<EventListener<?>>() {

			@Override
			public int compare(EventListener<?> o1, EventListener<?> o2) {
				return Integer.valueOf(o1.weight()).compareTo(o2.weight());
			}
			
		});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void fireEvent(EventListener listener, SystemEvent evt) {
		if(evt.async()) {
			eventExecutor.execute(()-> tenantService.execute(() -> listener.onEvent(evt)));

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
	
	@SuppressWarnings("rawtypes")
	@Override
	public void on(EventListener handler, String... resourceKeys) {
		
		for(String resourceKey : resourceKeys) {
			on(resourceKey, handler);
		}
		
	}

	@Override
	public <T extends UUIDEntity> void changed(Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		
		String resourceKey = templateService.getTemplateResourceKey(clz);
		on(handler, Events.updated(resourceKey), Events.deleted(resourceKey));
		
	}
	
	@Override
	public <T extends UUIDEntity> void any(Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		on(templateService.getTemplateResourceKey(clz), handler);
	}
	
	@Override
	public <T extends UUIDEntity> void on(String eventKey, Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		on(eventKey, handler);
	}
	
	@Override
	public <T extends UUIDEntity, E extends ObjectEvent<T>> void on(String eventKey, Class<E> eventClz, Class<T> clz, EventListener<E> handler) {
		on(eventKey, handler);
	}
	
	@Override
	public <T extends UUIDEntity> void created(Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		on(Events.created(templateService.getTemplateResourceKey(clz)), handler);
	}

	@Override
	public <T extends UUIDEntity> void updated(Class<T> clz, EventListener<ObjectUpdateEvent<T>> handler) {
		on(Events.updated(templateService.getTemplateResourceKey(clz)), handler);
	}
	
	@Override
	public <T extends UUIDEntity> void saved(Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		on(Events.created(templateService.getTemplateResourceKey(clz)), handler);
		on(Events.updated(templateService.getTemplateResourceKey(clz)), handler);
	}
	
	@Override
	public <T extends UUIDEntity> void saving(Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		on(Events.creating(templateService.getTemplateResourceKey(clz)), handler);
		on(Events.updating(templateService.getTemplateResourceKey(clz)), handler);
	}

	@Override
	public <T extends UUIDEntity> void deleted(Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		on(Events.deleted(templateService.getTemplateResourceKey(clz)), handler);
	}
	
	@Override
	public <T extends UUIDEntity> void creating(Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		on(Events.creating(templateService.getTemplateResourceKey(clz)), handler);
	}

	@Override
	public <T extends UUIDEntity> void updating(Class<T> clz, EventListener<ObjectUpdateEvent<T>> handler) {
		on(Events.updating(templateService.getTemplateResourceKey(clz)), handler);
	}

	@Override
	public <T extends UUIDEntity> void deleting(Class<T> clz, EventListener<ObjectEvent<T>> handler) {
		on(Events.deleting(templateService.getTemplateResourceKey(clz)), handler);
	}
	
	@Override
	public <T extends UUIDEntity> void assigned(Class<T> clz, EventListener<ObjectUpdateEvent<T>> handler) {
		on(Events.assigned(templateService.getTemplateResourceKey(clz)), handler);
	}
	
	@Override
	public <T extends UUIDEntity> void unassigned(Class<T> clz, EventListener<ObjectUpdateEvent<T>> handler) {
		on(Events.unassigned(templateService.getTemplateResourceKey(clz)), handler);
	}
	
	@Override
	public <T extends SystemEvent> void on(Class<T> clz, EventListener<T> handler) {
		on(templateService.getTemplateResourceKey(clz), handler);
	}

	@Override
	public void haltEvents() {
		disableThreadEvents.set(Boolean.TRUE);
	}

	@Override
	public void resumeEvents() {
		disableThreadEvents.remove();
	}
}
