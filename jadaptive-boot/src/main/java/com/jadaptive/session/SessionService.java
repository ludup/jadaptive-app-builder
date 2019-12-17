package com.jadaptive.session;

import com.jadaptive.tenant.AbstractTenantAwareObjectService;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.user.User;

public interface SessionService extends AbstractTenantAwareObjectService<Session> {

	boolean isLoggedOn(Session session, boolean touch);

	void closeSession(Session session);

	void touch(Session session);

	Session createSession(Tenant S, User user, String remoteAddress, String userAgent);

}
