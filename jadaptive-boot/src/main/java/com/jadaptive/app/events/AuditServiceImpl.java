package com.jadaptive.app.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.events.AuditEvent;
import com.jadaptive.api.events.AuditService;


@Service
public class AuditServiceImpl implements AuditService { 
	
	@Autowired
	private TenantAwareObjectDatabase<AuditEvent> eventDatabase;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Override
	public <T extends AuditEvent> void publishEvent(T evt) {
		eventDatabase.saveOrUpdate(evt);
		eventPublisher.publishEvent(new UUIDEntityEvent<T>(evt));
	}
}
