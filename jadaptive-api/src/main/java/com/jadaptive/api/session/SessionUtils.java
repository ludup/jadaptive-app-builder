package com.jadaptive.api.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Utils;

@Component
public class SessionUtils {

	static Logger log = LoggerFactory.getLogger(SessionUtils.class);
	
	static boolean debugCSRF = "true".equals(System.getProperty("jadaptive.csrfDebugRequests"));

	public static final String AUTHENTICATED_SESSION = "authenticatedSession";
	public static final String SESSION_COOKIE = "JADAPTIVE_SESSION";
	
	public static final String USER_LOCALE = "userLocale";
	public static final String LOCALE_COOKIE = "JADAPTIVE_LOCALE";

	public static final String CSRF_TOKEN_ATTRIBUTE = "csrftoken";

	public static final String PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz";
	 
	static ThreadLocal<User> threadUsers = new ThreadLocal<>();
	
	@Autowired
	private SessionService sessionService;; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private SingletonObjectDatabase<SessionConfiguration> sessionConfig; 
	
	public static final String UNSAFE_INLINE = "unsafe-inline";
	
	public User getCurrentUser() {
		return permissionService.getCurrentUser();
	}
	
	public Session getActiveSession(HttpServletRequest request) {
		
		Session session = null;
		
		permissionService.setupSystemContext();
		
		try {
			
			if(request.getParameterMap().containsKey(SESSION_COOKIE)) {
				session = sessionService.getSession(request.getParameter(SESSION_COOKIE));
			} else if(request.getHeader(SESSION_COOKIE) != null) {
				session = sessionService.getSession((String)request.getHeader(SESSION_COOKIE));
			}
			
			if(Objects.nonNull(request.getCookies())) {
				for (Cookie c : request.getCookies()) {
					if (c.getName().equals(SESSION_COOKIE)) {
						session = sessionService.getSession(c.getValue());
						if (session != null) {
							break;
						}
					}
				}
			}
			
			if (session != null && sessionService.isLoggedOn(session, true)) {
				return session;
			}
			
		} catch(UnauthorizedException | ObjectNotFoundException e) { 

		} finally {
			permissionService.clearUserContext();
		}

		return null;
	}

	public Session touchSession(HttpServletRequest request,
			HttpServletResponse response, Session session) {

		// Preserve the session for future lookups in this request and session
		request.setAttribute(AUTHENTICATED_SESSION, session);
		request.getSession().setAttribute(AUTHENTICATED_SESSION, session);

		addSessionCookies(request, response, session);

		return session;

	}

	public Session getSession(HttpServletRequest request)
			throws UnauthorizedException, SessionTimeoutException {

		/**
		 * This method SHOULD NOT touch the session.
		 */
		Session session = null;
		
		if(request.getParameterMap().containsKey(SESSION_COOKIE)) {
			session = sessionService.getSession(request.getParameter(SESSION_COOKIE));
		} else if(request.getHeader(SESSION_COOKIE) != null) {
			session = sessionService.getSession((String)request.getHeader(SESSION_COOKIE));
		}
		
		if (session != null && sessionService.isLoggedOn(session, false)) {
			return session;
		}
		
		if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}
		
		if (request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getSession().getAttribute(
					AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}
		
		if(request.getCookies()!=null) {
			for (Cookie c : request.getCookies()) {
				if (c.getName().equals(SESSION_COOKIE)) {
					session = sessionService.getSession(c.getValue());
					if (session != null && sessionService.isLoggedOn(session, false)) {
						return session;
					}
				}
			}
		}
		
		throw new UnauthorizedException();
	}

	
	public void verifySameSiteRequest(HttpServletRequest request) throws UnauthorizedException {
		
		SessionConfiguration config = sessionConfig.getObject(SessionConfiguration.class);
		
		if(config.getEnableCsrf()) {
			String requestToken = request.getParameter(CSRF_TOKEN_ATTRIBUTE);
			if(Objects.isNull(requestToken)) {
				requestToken = request.getHeader("CsrfToken");
			}
			String csrf = (String)request.getSession().getAttribute(CSRF_TOKEN_ATTRIBUTE);
			if(Objects.isNull(csrf)) {
				log.warn("No CSRF token in session!");
				return;
			}
			if(Objects.isNull(requestToken)) {
				throw new UnauthorizedException("No CSRF token in form!");
			}
			if(!requestToken.equals(csrf)) {
				log.warn("CSRF token mistmatch from {}", request.getRequestURI());
				log.debug("REMOVEME: Current token {}", csrf);
				log.debug("REMOVEME: Request token {}", requestToken);
				debugRequest(request);
				throw new UnauthorizedException(String.format("CSRF token mistmatch from %s", 
						request.getRequestURI()));
			}
		}
	}

	protected void debugRequest(HttpServletRequest request) {
		if(debugCSRF) {
			log.warn(String.format("The request URI was %s, and contained the following parameters :-",request.getRequestURI()));
			for(Map.Entry<String, String[]> en : request.getParameterMap().entrySet()) {
				log.warn(String.format("  %s = %s", en.getKey(), String.join(",", en.getValue())));
			}
			log.warn("And the following headers :-");
			for(Enumeration<String> hdrEnum = request.getHeaderNames(); hdrEnum.hasMoreElements(); ) {
				String hdr = hdrEnum.nextElement();
				log.warn(String.format("  %s = %s", hdr, request.getHeader(hdr)));
			}
		}
	}

	public boolean isValidCORSRequest(HttpServletRequest request, HttpServletResponse response, Properties properties) {
		
		String requestOrigin = request.getHeader("Origin");
		
		if(Objects.nonNull(requestOrigin)) {
			if(log.isDebugEnabled()) {
				log.debug("CORS request for origin {}", requestOrigin);
			}
		} else if(Objects.isNull(requestOrigin)) {
			return false;
		}
		
		boolean pathAllowsCORS = Boolean.parseBoolean(properties.getProperty("cors.enabled", "true"));
		if(!pathAllowsCORS) {
			if(log.isDebugEnabled()) {
				log.debug("Security properties of URI {} explicitly deny CORS", request.getRequestURI());
			}
			return false;
		}
		
		List<String> origins = new ArrayList<>();
		origins.addAll(Arrays.asList(properties.getProperty("cors.origins", "").split(",")));
		
		if(origins.size() > 0) {
			if(log.isDebugEnabled()) {
				log.debug("Security properties allows origins {}", Utils.csv(origins));
			}
		}
		
		if(ApplicationProperties.getValue("cors.enabled", false) && !Objects.isNull(requestOrigin)) {
			
			List<String> tmp =  new ArrayList<>();
			tmp.addAll(Arrays.asList(ApplicationProperties.getValue("cors.origins", "").split(",")));
			
			if(tmp.size() > 0) {
				if(log.isDebugEnabled()) {
					log.debug("Global configuration allows origins {}", Utils.csv(tmp));
				}
			}	
			
			origins.addAll(tmp);
		}

		if(origins.contains(requestOrigin)) {
			if(log.isDebugEnabled()) {
				log.debug("Origin {} allowed for URI {}", requestOrigin, request.getRequestURI());
			}
			/**
			 * Allow CORS to this realm. We must allow credentials as the
			 * API will be useless without them.
			 */
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.addHeader("Access-Control-Allow-Origin", requestOrigin);
			
			return true;
		}
		
		return false;
	}

	public void addSessionCookies(HttpServletRequest request,
			HttpServletResponse response, Session session) {

		if(Boolean.TRUE.equals(request.getAttribute("processedCookies"))) {
			return;
		}
		
		Cookie cookie = new Cookie(SESSION_COOKIE, session.getUuid());
		cookie.setMaxAge((session.getSessionTimeout() > 0 ? 60 * session.getSessionTimeout() : Integer.MAX_VALUE));
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		addCookie(cookie, response);
		
		request.setAttribute("processedCookies", Boolean.TRUE);
	
	}
	
	public void addCookie(Cookie cookie, HttpServletResponse response) {

		StringBuffer cookieHeader = new StringBuffer();

		cookieHeader.append(cookie.getName());
		cookieHeader.append("=");
		
		/**
		 * Make sure we are not adding duplicate cookies
		 */
		for(String entry : response.getHeaderNames()) {
			if(entry.equalsIgnoreCase("Set-Cookie") && response.getHeader(entry).startsWith(cookieHeader.toString())) {
				return;
			}
		}
		
		cookieHeader.append(cookie.getValue());
		if (cookie.getPath() != null) {
			cookieHeader.append("; Path=");
			cookieHeader.append(cookie.getPath());
		}
		if (cookie.getDomain() != null) {
			cookieHeader.append("; Domain=");
			cookieHeader.append(cookie.getDomain());
		}
		if (cookie.getMaxAge() > 0) {
			cookieHeader.append("; Max-Age=");
			cookieHeader.append(cookie.getMaxAge());
			/**
			 * This breaks IE when date of server and browser do not match
			 */
			cookieHeader.append("; Expires=");
			if (cookie.getMaxAge() == 0) {
				cookieHeader.append(Utils.formatDate(new Date(10000), PATTERN_RFC1036));
			} else {
				cookieHeader.append(Utils.formatDate(new Date(System
						.currentTimeMillis() + cookie.getMaxAge() * 1000L), PATTERN_RFC1036));
			}
		}
		
		if (cookie.getSecure()) {
			cookieHeader.append("; Secure");
		}
		
		
		if (cookie.isHttpOnly()) { 
			cookieHeader.append("; HttpOnly"); 
		}
		cookieHeader.append("; SameSite=Lax");

		response.addHeader("Set-Cookie", cookieHeader.toString());
		
	}

	public Locale getLocale(HttpServletRequest request) {

		if (request.getSession().getAttribute(USER_LOCALE) == null) {

			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals(LOCALE_COOKIE)) {
					return new Locale(c.getValue());
				}
			}
			return Locale.getDefault();
		} else {
			return new Locale((String) request.getSession().getAttribute(
					USER_LOCALE));
		}

	}

	public void setLocale(HttpServletRequest request,
			HttpServletResponse response, String locale) {

		request.getSession().setAttribute(USER_LOCALE, locale);

		Cookie cookie = new Cookie(LOCALE_COOKIE, locale);
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		if(request.getProtocol().equalsIgnoreCase("https")) {
			cookie.setSecure(true);
		} else {
			cookie.setSecure(false);
			cookie.setHttpOnly(true);
		}
		cookie.setDomain(request.getServerName());
		addCookie(cookie, response);

	}

	public void touchSession(Session session) throws SessionTimeoutException {

		if (!sessionService.isLoggedOn(session, true)) {
			throw new SessionTimeoutException();
		}
	}
	
	public void touch(Session session) {
		sessionService.touch(session);
	}

	public boolean hasActiveSession(HttpServletRequest request) {
		try {
			return getActiveSession(request)!=null;
		} catch (ObjectNotFoundException e) {
			return false;
		}
	}

	public User getCurrentUser(HttpServletRequest request) throws UnauthorizedException, SessionTimeoutException {
		return getSession(request).getUser();
	}

	public String setupCSRFToken(HttpServletRequest request) {
		String token = (String) Utils.generateRandomAlphaNumericString(64);
		request.getSession().setAttribute(CSRF_TOKEN_ATTRIBUTE, token);
		if(log.isDebugEnabled()) {
			log.debug("REMOVEME: Changed CSRF token to {}", token);
		}
		return token;
	}

	public void populateSecurityHeaders(HttpServletResponse response) {
		
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
		response.setHeader("Content-Security-Policy", 
				"default-src 'none'; font-src 'self'; script-src 'self'; style-src 'self'; connect-src 'self'; img-src 'self' data: https://www.gravatar.com/; object-src 'self'; frame-ancestors 'self';");
	}

	public void setDoNotCache(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store");
	}
	
	public void setCachable(HttpServletResponse response, int age) {
		response.setHeader("Cache-Control", "max-age=" + age + ", must-revalidate");
	}
	
	public void addContentSecurityPolicy(HttpServletResponse response, String policy, String value) {
		Collection<String> csp = response.getHeaders("Content-Security-Policy");
		if(csp.isEmpty()) {
			populateSecurityHeaders(response);
		}
		String header = csp.iterator().next();
		int idx = header.indexOf(policy);
		if(idx > -1) {
			int idx2 = header.indexOf(';', idx);
			String tmp = header.substring(idx, idx2);
			if(tmp.contains(String.format("'%s'", value))) {
				return;
			}
			header = header.replace(policy, String.format("%s '%s'", policy, value));
			
		} else {
			header = header + String.format(" %s '%s';", policy, value);
		}
		response.setHeader("Content-Security-Policy", header);
	}
	
	public void addScriptNoncePolicy(HttpServletResponse response, String nonce) {
		addContentSecurityPolicy(response, "script-src", String.format("nonce-%s", nonce));
	}

}
