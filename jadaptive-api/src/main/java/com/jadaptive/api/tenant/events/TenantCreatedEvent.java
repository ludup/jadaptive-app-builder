package com.jadaptive.api.tenant.events;

import com.jadaptive.api.events.AbstractUUIDEntityEvent;
import com.jadaptive.api.events.DefaultEventType;
import com.jadaptive.api.tenant.Tenant;

public class TenantCreatedEvent extends AbstractUUIDEntityEvent<Tenant> {

	private static final long serialVersionUID = 1L;

	public TenantCreatedEvent(Object source, Tenant obj) {
		super(source, DefaultEventType.created, obj);
	}
	
	public TenantCreatedEvent(Object source, Tenant obj, Throwable e) {
		super(source, DefaultEventType.created, obj, e);
	}

}
