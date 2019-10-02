package com.jadaptive.tenant.events;

import com.jadaptive.events.DefaultEventType;
import com.jadaptive.events.AbstractUUIDEntityEvent;
import com.jadaptive.tenant.Tenant;

public class TenantUpdatedEvent extends AbstractUUIDEntityEvent<Tenant> {

	private static final long serialVersionUID = 1L;

	public TenantUpdatedEvent(Object source, Tenant obj) {
		super(source, DefaultEventType.updated, obj);
	}

}
