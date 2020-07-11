package com.jadaptive.app.auth;

import java.util.Objects;

import javax.cache.Cache;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
@Permissions(keys = { AuthenticationService.USER_LOGIN_PERMISSION }, defaultPermissions = { AuthenticationService.USER_LOGIN_PERMISSION } )
public class AuthenticationServiceImpl extends AuthenticatedService implements AuthenticationService {

	static Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private CacheService cacheService; 
	@Override
	public Session logonUser(String username, String password, 
			Tenant tenant, String remoteAddress, String userAgent) {
		
		permissionService.setupSystemContext();
		
		try {
			
			assertLoginThreshold(username);
			assertLoginThreshold(remoteAddress);
			
			User user = userService.getUser(username);
			if(Objects.isNull(user) ||!userService.supportsLogin(user)) {
				throw new AccessDeniedException("Bad username or password");
			}
			
			setupUserContext(user);
			
			try {
				
				assertPermission(USER_LOGIN_PERMISSION);
				
				if(!userService.verifyPassword(user, password.toCharArray())) {
					flagFailedLogin(username);
					flagFailedLogin(remoteAddress);
					
					if(log.isInfoEnabled()) {
						log.info("Flagged failed login from {} and {}", username, remoteAddress);
					}
					
					throw new AccessDeniedException("Bad username or password");
				}
				
				return sessionService.createSession(tenant, 
						user, remoteAddress, userAgent);
				
			} finally {
				clearUserContext();
			}
			
		
		} finally {
			permissionService.clearUserContext();
		}
	}
	
	private void assertLoginThreshold(String key) {
		Integer count = getCache().get(getCacheKey(key));
		if(Objects.nonNull(count)) {
			if(count > getFailedLoginThreshold()) {
				if(log.isInfoEnabled()) {
					log.info("Rejecting login due to too many failues from {}", key);
				}
				throw new AccessDeniedException("Too many failed login attempts");
			}
		}
	}
	
	private Integer getFailedLoginThreshold() {
		return 10;
	}
	
	private Cache<String, Integer> getCache() {
		return cacheService.getCacheOrCreate("failedLogings", 
				String.class, Integer.class, 
					CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
	}
	
	private String getCacheKey(String username) {
		return String.format("%s.%s", getCurrentTenant().getUuid(), username);
	}

	private void flagFailedLogin(String username) {
		Cache<String, Integer> cache = getCache();
		String cacheKey = getCacheKey(username);
		Integer count = cache.get(cacheKey);
		if(Objects.isNull(count)) {
			count = new Integer(0);
		}
		cache.put(cacheKey, ++count);
	}
}
