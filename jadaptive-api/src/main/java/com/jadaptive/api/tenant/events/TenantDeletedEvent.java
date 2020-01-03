package com.jadaptive.tenant.events;

import com.jadaptive.events.DefaultEventType;
import com.jadaptive.events.AbstractUUIDEntityEvent;
import com.jadaptive.tenant.Tenant;

public class TenantDeletedEvent extends AbstractUUIDEntityEvent<Tenant> {

	private static final long serialVersionUID = 1L;

	public TenantDeletedEvent(Object source, Tenant obj) {
		super(source, DefaultEventType.deleted, obj);
	}

}
