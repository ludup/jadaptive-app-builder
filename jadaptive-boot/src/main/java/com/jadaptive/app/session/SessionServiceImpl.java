package com.jadaptive.app.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionConfiguration;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionState;
import com.jadaptive.api.session.SessionType;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.session.events.SessionClosedEvent;
import com.jadaptive.api.session.events.SessionOpenedEvent;
import com.jadaptive.api.stats.UsageService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.Utils;

@Service
public class SessionServiceImpl extends AuthenticatedService implements SessionService {

	static Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);
	
//	@Autowired
//	private TenantAwareObjectDatabase<Session> repository;
	
	@Autowired
	private SingletonObjectDatabase<SessionConfiguration> configService;
	
	@Autowired
	private EventService eventService; 
	
	@Autowired
	private UserService userService; 
	
	@Autowired
	private UsageService usageService;
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private CacheService cacheService;
	
	@Override
	public Session createSession(Tenant tenant, User user, String remoteAddress, String userAgent, SessionType type) {
		
		SessionConfiguration sessionConfig = configService.getObject(SessionConfiguration.class);
		
		Session session = new Session();
		session.setUuid(UUID.randomUUID().toString());
		session.setRemoteAddress(remoteAddress);
		session.setType(type);
		session.setSessionTimeout(sessionConfig.getTimeout());
		session.setSignedIn(new Date());
		session.setTenant(tenant);
		session.setUserAgent(userAgent);
		session.setUser(user);
		session.setState(SessionState.ACTIVE);

//		repository.saveOrUpdate(session);
		getCache().put(session.getUuid(), session);
		
		user.setLastLogin(Utils.now());
		eventService.haltEvents();
		userService.saveOrUpdate(user);
		eventService.resumeEvents();
		
		tenantService.executeAs(tenant, ()->eventService.publishEvent(new SessionOpenedEvent(session)));
		return session;
	}
	
	protected Map<String,Session> getCache() {
		return cacheService.getCacheOrCreate("sessions", String.class, Session.class);
	}

	@Override
	public boolean isLoggedOn(Session session, boolean touch) {
		
		setupSystemContext();

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
						
						try {
							session = getSession(session.getUuid());
							
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
						} catch (UnauthorizedException e) {
							return false;
						}
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
			getCache().put(session.getUuid(), session);
		}
	}

	@Override
	public void closeSession(Session session) {

		log.info("Closing session " + session.getUser().getUsername() + "/"
				+ session.getUuid() 
				+ " lastUpdated=" + 
					(Objects.nonNull(session.getLastUpdated()) ? session.getLastUpdated().toString() : "") 
						+ " timeout=" + session.getSessionTimeout());
		
		if (session.getSignedOut() != null) {
			log.error("Attempting to close a session which is already closed!");
			return;
		}

		session.setState(SessionState.EXPIRED);
		session.setSignedOut(new Date());
		getCache().put(session.getUuid(), session);
		usageService.log(session.getSignedOut().getTime() - session.getSignedIn().getTime(),
				SESSION_USAGE, session.getUser().getUuid());
		
		tenantService.executeAs(session.getTenant(), ()->eventService.publishEvent(new SessionClosedEvent(session)));

	}

	@Override
	public Session getSession(String uuid) throws UnauthorizedException {
		
		Session session = getCache().get(uuid);
		if(Objects.isNull(session)) {
			throw new UnauthorizedException();
		}
		return session;
	}

	@Override
	public Iterable<Session> iterateSessions() {
		List<Session> activeSessions = new ArrayList<>();
		for(Session session : getCache().values()) {
			if(!session.isClosed()) {
				activeSessions.add(session);
			}
		}
		return activeSessions;
	}

	@Override
	public Iterable<Session> inactiveSessions() {
		List<Session> inactiveSessions = new ArrayList<>();
		for(Session session : getCache().values()) {
			if(session.isClosed()) {
				inactiveSessions.add(session);
			}
		}
		return inactiveSessions;
	}

	@Override
	public void deleteSession(Session session) {	
		getCache().remove(session.getUuid());
	}

	@Override
	public Session getObjectByUUID(String uuid) {
		Session session = getCache().get(uuid);
		if(Objects.isNull(session)) {
			throw new ObjectNotFoundException("No session with uuid " + uuid);
		}
		return session;
	}

	@Override
	public String saveOrUpdate(Session object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteObject(Session object) {
		deleteSession(object);
	}

	@Override
	public void deleteObjectByUUID(String uuid) {
		deleteSession(getObjectByUUID(uuid));
	}

	@Override
	public Iterable<Session> allObjects() {
		return Collections.unmodifiableCollection(getCache().values());
	}

}
