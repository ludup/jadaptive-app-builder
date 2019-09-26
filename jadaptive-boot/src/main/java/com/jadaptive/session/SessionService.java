package com.jadaptive.session;

import com.jadaptive.tenant.AbstractTenantAwareObjectService;

public interface SessionService extends AbstractTenantAwareObjectService<Session> {

	boolean isLoggedOn(Session session, boolean touch);

	void closeSession(Session session);

	void touch(Session session);

}
