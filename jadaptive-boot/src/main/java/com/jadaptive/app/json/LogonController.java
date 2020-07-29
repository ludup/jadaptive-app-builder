package com.jadaptive.app.json;

import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.session.SessionFilter;

@Controller
public class LogonController {

	static Logger log = LoggerFactory.getLogger(LogonController.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private SecurityPropertyService securityService; 
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private SessionService sessionService; 
	
	@RequestMapping(value="/app/api/logon/basic", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public SessionStatus logonUser(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam String username, @RequestParam String password)  {

		permissionService.setupSystemContext();
		
		try {
			Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
			
			if("true".equalsIgnoreCase(properties.getProperty("logon.basic.disabled"))) {
				return new SessionStatus("Permission denied");
			}
			
			Session session = authenticationService.logonUser(username, password,
					tenantService.getCurrentTenant(), 
					request.getRemoteAddr(), 
					request.getHeader(HttpHeaders.USER_AGENT));
			
			sessionUtils.addSessionCookies(request, response, session);
			
			String homePage = (String) request.getSession().getAttribute(SessionFilter.PRE_LOGON_ORIGINAL_URL);
			if(Objects.isNull(homePage)) {
				homePage = properties.getProperty("authentication.homePage");
			}
			if(log.isInfoEnabled()) {
				log.info("Logged user {} on with home {}", username, StringUtils.defaultIfBlank(homePage, "default"));
			}
			return new SessionStatus(session, homePage);
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/logon/basic", e);
			}
			return new SessionStatus(e.getMessage());
		} finally {
			permissionService.clearUserContext();
		}
	}
	
	@RequestMapping(value="/app/api/logoff", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus logoff(HttpServletRequest request, HttpServletResponse response)  {

		try {
			Session session = sessionUtils.getSession(request);
			if(session.isClosed()) {
				return new RequestStatus(false, "Session already closed");
			}
			
			sessionService.closeSession(session);
			
			return new RequestStatus(true, "Session closed");
		} catch(UnauthorizedException | SessionTimeoutException e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/session/touch", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus touchSession(HttpServletRequest request, HttpServletResponse response)  {

		try {
			Session session = sessionUtils.getSession(request);
			if(!sessionService.isLoggedOn(session, true)) {
				return new RequestStatus(false, "Session closed");
			}
			
			return new RequestStatus(true, "");
		} catch(UnauthorizedException | SessionTimeoutException e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
}
