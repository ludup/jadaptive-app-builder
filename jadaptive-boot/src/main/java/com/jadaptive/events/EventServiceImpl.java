package com.jadaptive.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


@Service
public class EventServiceImpl implements EventService {

	@Autowired
	ApplicationEventPublisher eventPublisher;  
	
	@Override
	public void publishEvent(SystemEvent evt) {
		eventPublisher.publishEvent(evt);
	}
}
