package com.jadaptive.app.session;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionRepository;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

@Service
public class SessionServiceImpl implements SessionService {

	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
	
	@Autowired
	private SessionRepository repository;
	
	@Override
	public Session createSession(Tenant tenant, User user, String remoteAddress, String userAgent) {
		
		Session session = new Session();
		session.setRemoteAddress(remoteAddress);
		session.setSessionTimeout(60 * 15);
		session.setSignedIn(new Date());
		session.setTenant(tenant);
		session.setUserAgent(userAgent);
		session.setUsername(user.getUsername());
		
		repository.saveOrUpdate(session);
		
		return session;
	}

	@Override
	public boolean isLoggedOn(Session session, boolean touch) {
		
		session = repository.get(session.getUuid());
		
		if (session.getSignedOut() == null) {

			if(session.getSessionTimeout() > 0) {
				Calendar currentTime = Calendar.getInstance();
				Calendar c = Calendar.getInstance();
				if(session.getLastUpdated()!=null) {
					c.setTime(session.getLastUpdated());
					c.add(Calendar.MINUTE, session.getSessionTimeout());
				}
				if (log.isDebugEnabled()) {
					log.debug("Checking session timeout currentTime="
							+ currentTime.getTime() + " lastUpdated="
							+ session.getLastUpdated() + " timeoutThreshold="
							+ c.getTime());
				}
	
				if (c.before(currentTime)) {
					if (log.isDebugEnabled()) {
						log.debug("Session has timed out");
					}
					closeSession(session);
	
					if (log.isDebugEnabled()) {
						log.debug("Session "
								+ session.getUsername() + "/"
								+ session.getUuid() + " is now closed");
					}
	
					return false;
				}
			}
			if (touch) {
				touch(session);
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void touch(Session session) {
		if(session.isReadyForUpdate()) {
			session.setLastUpdated(new Date());
			repository.saveOrUpdate(session);
		}
	}

	@Override
	public void closeSession(Session session) {

		if (session.getSignedOut() != null) {
			log.error("Attempting to close a session which is already closed!");
			return;
		}

		session.setSignedOut(new Date());
		repository.saveOrUpdate(session);

	}

	@Override
	public Session getSession(String uuid) {
		return repository.get(uuid);
	}

}
