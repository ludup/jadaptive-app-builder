package com.jadaptive.api.tenant.events;

import com.jadaptive.api.events.AbstractUUIDEntityEvent;
import com.jadaptive.api.events.DefaultEventType;
import com.jadaptive.api.tenant.Tenant;

public class TenantUpdatedEvent extends AbstractUUIDEntityEvent<Tenant> {

	private static final long serialVersionUID = 1L;

	public TenantUpdatedEvent(Object source, Tenant obj) {
		super(source, DefaultEventType.updated, obj);
	}

}
