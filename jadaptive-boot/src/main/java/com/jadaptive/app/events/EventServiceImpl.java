package com.jadaptive.app.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.SystemEvent;


@Service
public class EventServiceImpl implements EventService {

	@Autowired
	ApplicationEventPublisher eventPublisher;  
	
	@Override
	public void publishEvent(SystemEvent evt) {
		eventPublisher.publishEvent(evt);
	}
}
