package com.jadaptive.app.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.events.CustomEvent;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.EventType;
import com.jadaptive.api.repository.UUIDEntity;


@Service
public class EventServiceImpl implements EventService { 
	
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Override
	public <T extends UUIDEntity> void publishStandardEvent(EventType type, T evt) {
		switch(type) {
		case CREATE:
			eventPublisher.publishEvent(new UUIDEntityCreatedEvent<T>(evt));
			break;
		case READ:
			eventPublisher.publishEvent(new UUIDEntityReadEvent<T>(evt));
			break;
		case UPDATE:
			eventPublisher.publishEvent(new UUIDEntityUpdatedEvent<T>(evt));
			break;
		case DELETE:
			eventPublisher.publishEvent(new UUIDEntityDeletedEvent<T>(evt));
			break;
		}
	}

	@Override
	public <T extends CustomEvent> void publishCustomEvent(T evt) {
		
		
	}

	@Override
	public void publishDocumentEvent(EventType type, AbstractObject e) {
		
	}
}
