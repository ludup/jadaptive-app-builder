package com.jadaptive.app.session;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionConfiguration;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.session.events.SessionClosedEvent;
import com.jadaptive.api.session.events.SessionOpenedEvent;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Utils;

@Service
public class SessionServiceImpl extends AuthenticatedService implements SessionService {

	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
	
	@Autowired
	private TenantAwareObjectDatabase<Session> repository;
	
	@Autowired
	private SingletonObjectDatabase<SessionConfiguration> configService;
	
	@Autowired
	private EventService eventService; 
	
	@Override
	public Session createSession(Tenant tenant, User user, String remoteAddress, String userAgent) {
		
		SessionConfiguration sessionConfig = configService.getObject(SessionConfiguration.class);
		
		Session session = new Session();
		session.setRemoteAddress(remoteAddress);
		session.setSessionTimeout(sessionConfig.getTimeout());
		session.setSignedIn(new Date());
		session.setTenant(tenant);
		session.setUserAgent(userAgent);
		session.setUser(user);
		session.setCsrfToken(Utils.generateRandomAlphaNumericString(32));
		
		repository.saveOrUpdate(session);
		
		eventService.publishEvent(new SessionOpenedEvent(session));
		return session;
	}

	@Override
	public boolean isLoggedOn(Session session, boolean touch) {
		
		session = repository.get(session.getUuid(), Session.class);
		
		setupUserContext(session.getUser());
		
		try {
			if (session.getSignedOut() == null) {
	
				if(session.getSessionTimeout() > 0) {
					Calendar currentTime = Calendar.getInstance();
					Calendar c = Calendar.getInstance();
					if(session.getLastUpdated()!=null) {
						c.setTime(session.getLastUpdated());
						c.add(Calendar.MINUTE, session.getSessionTimeout());
					}
					if (log.isTraceEnabled()) {
						log.trace("Checking session timeout currentTime="
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
									+ session.getUser().getUsername() + "/"
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
		} finally {
			clearUserContext();
		}
	}
	
	@Override
	public void touch(Session session) {
		if(session.isReadyForUpdate()) {
			if(log.isDebugEnabled()) {
				log.debug("Touching " + session.getUser().getUsername() + "/"
						+ session.getUuid() + " " + 
							(Objects.nonNull(session.getLastUpdated()) ? session.getLastUpdated().toString() : "") 
								+ " timeout=" + session.getSessionTimeout());
			}
			session.setLastUpdated(new Date());
			repository.saveOrUpdate(session);
		}
	}

	@Override
	public void closeSession(Session session) {

		log.info("Closing session " + session.getUser().getUsername() + "/"
				+ session.getUuid() + " " + 
					(Objects.nonNull(session.getLastUpdated()) ? session.getLastUpdated().toString() : "") 
						+ " timeout=" + session.getSessionTimeout());
		
		if (session.getSignedOut() != null) {
			log.error("Attempting to close a session which is already closed!");
			return;
		}

		session.setSignedOut(new Date());
		repository.saveOrUpdate(session);
		
		eventService.publishEvent(new SessionClosedEvent(session));

	}

	@Override
	public Session getSession(String uuid) throws UnauthorizedException {
		try {
		return repository.get(uuid, Session.class);
		} catch(ObjectNotFoundException e) {
			throw new UnauthorizedException();
		}
	}

	@Override
	public Iterable<Session> iterateSessions() {
		return repository.list(Session.class, SearchField.eq("signedOut", null));
	}

}
