package com.jadaptive.api.events;

public interface AuditService {

	<T extends AuditEvent> void publishEvent(T evt);

}
