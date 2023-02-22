package com.jadaptive.api.session;

import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

public interface SessionService extends UUIDObjectService<Session> {

	public static final String SESSION_USAGE = "sessionTimeMs";
	
	boolean isLoggedOn(Session session, boolean touch);

	void closeSession(Session session);

	void touch(Session session);

	Session getSession(String uuid) throws UnauthorizedException;

	Iterable<Session> iterateSessions();

	Session createSession(Tenant tenant, User user, String remoteAddress, String userAgent, SessionType type);

}
