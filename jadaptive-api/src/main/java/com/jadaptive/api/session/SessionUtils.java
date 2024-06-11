package com.jadaptive.api.session;

import java.io.Closeable;
import java.io.IOException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.ParameterHelper;
import com.jadaptive.utils.Utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SessionUtils {
	
	@FunctionalInterface
	public interface IoRunnable {
		void runIo() throws IOException;
	}

	public static Closeable scopedIoWithoutSessionTimeout(HttpServletRequest request) throws IOException {
		var session = request.getSession(false);
		var was = -1;
		if(session != null) {
			was = session.getMaxInactiveInterval();
			session.setMaxInactiveInterval(0);
		}
		var fwas = was;
		return new Closeable() {
			@Override
			public void close() throws IOException {
				if(session != null) {
					session.setMaxInactiveInterval(fwas);
				}
			}
		};
	}
	
	public static void runIoWithoutSessionTimeout(HttpServletRequest request, IoRunnable r) throws IOException {
		var session = request.getSession(false);
		var was = -1;
		if(session != null) {
			was = session.getMaxInactiveInterval();
			session.setMaxInactiveInterval(0);
		}
		try {
			r.runIo();
		}
		finally {
			if(session != null) {
				session.setMaxInactiveInterval(was);
			}
		}
	}

	static Logger log = LoggerFactory.getLogger(SessionUtils.class);
	
	static boolean debugCSRF = "true".equals(System.getProperty("jadaptive.csrfDebugRequests"));

	//public static final String AUTHENTICATED_SESSION = "authenticatedSession";
	
	public static final String SESSION_ID = "sessionId";
	
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

	private static final String DISABLE_CONTENT_SECURITY = "disableCSP";
	
	public User getCurrentUser() {
		return permissionService.getCurrentUser();
	}

	public int getTimeout() {
		return sessionConfig.getObject(SessionConfiguration.class).getTimeout();
	}
	
	public void verifySameSiteRequest(HttpServletRequest request) throws UnauthorizedException {
		verifySameSiteRequest(request, request.getParameterMap());
	}
	
	public void verifySameSiteRequest(HttpServletRequest request, Map<String,String[]> parameters) throws UnauthorizedException {
		
		SessionConfiguration config = sessionConfig.getObject(SessionConfiguration.class);
		
		if(config.getEnableCsrf()) {
			String requestToken = ParameterHelper.getValue(parameters, CSRF_TOKEN_ATTRIBUTE);
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

	public Locale getLocale(jakarta.servlet.http.HttpServletRequest request) {

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

	public void touch(Session session) throws SessionTimeoutException  {
		sessionService.touch(session);
	}

	public User getCurrentUser(HttpServletRequest request) throws UnauthorizedException, SessionTimeoutException {
		return Session.get(request).getUser();
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
		
		if(Objects.isNull(Request.get().getAttribute(DISABLE_CONTENT_SECURITY))) {
			response.setHeader("X-Content-Type-Options", "nosniff");
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
			response.setHeader("Content-Security-Policy", 
					"default-src 'none'; font-src 'self' https://fonts.gstatic.com/; script-src 'self'; style-src 'self' https://fonts.googleapis.com/; connect-src 'self'; img-src 'self' data: https://www.gravatar.com/; object-src 'self'; frame-ancestors 'self';");
		}
	}

	public void setDoNotCache(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store");
	}
	
	public void setCachable(HttpServletResponse response, int age) {
		if(!Boolean.getBoolean("jadaptive.development") || Boolean.getBoolean("jadaptive.cachable")) {
			response.setHeader("Cache-Control", "max-age=" + age + ", must-revalidate");
		}
	}
	
	public void addContentSecurityPolicy(HttpServletResponse response, String policy, String value) {
		
		if(Objects.isNull(Request.get().getAttribute(DISABLE_CONTENT_SECURITY))) {
			Collection<String> csp = response.getHeaders("Content-Security-Policy");
			if(csp.isEmpty()) {
				populateSecurityHeaders(response);
				csp = response.getHeaders("Content-Security-Policy");
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
	}
	
	public void addScriptNoncePolicy(HttpServletResponse response, String nonce) {
		addContentSecurityPolicy(response, "script-src", String.format("nonce-%s", nonce));
	}

	public void disableContentSecurityPolicy() {
		
		Request.get().setAttribute(DISABLE_CONTENT_SECURITY, Boolean.TRUE);
	}

}
