package com.jadaptive.api.session;

import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

public interface SessionService {

	boolean isLoggedOn(Session session, boolean touch);

	void closeSession(Session session);

	void touch(Session session);

	Session createSession(Tenant S, User user, String remoteAddress, String userAgent);

	Session getSession(String uuid);

	Iterable<Session> iterateSessions();

}
