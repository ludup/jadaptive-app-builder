package com.jadaptive.app;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.triggers.TriggerService;

@Service
public class TriggerServiceImpl extends AuthenticatedService implements TriggerService {

	@EventListener
	@Override
	public void onAuditEvent(SystemEvent<?> evt) {
		
		System.out.println("Processing audit event " + evt.getResourceKey());
	}
}
