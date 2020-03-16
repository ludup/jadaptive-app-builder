package com.jadaptive.app.json;

import java.util.Objects;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.auth.AuthenticationService;
import com.jadaptive.app.auth.SessionInterceptor;
import com.jadaptive.app.session.SessionUtils;

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
	
	@RequestMapping(value="api/logon", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus logonUser(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam String username, @RequestParam String password)  {

		permissionService.setupSystemContext();
		
		try {
			Session session = authenticationService.logonUser(username, password,
					tenantService.getCurrentTenant(), 
					request.getRemoteAddr(), 
					request.getHeader(HttpHeaders.USER_AGENT));
			
			sessionUtils.addSessionCookies(request, response, session);
			
			String homePage = (String) request.getSession().getAttribute(SessionInterceptor.PRE_LOGON_ORIGINAL_URL);
			if(Objects.isNull(homePage)) {
				Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
				homePage = properties.getProperty("authentication.homePage");
			}
			if(log.isInfoEnabled()) {
				log.info("Logged user {} on with home {}", username, homePage);
			}
			return new RequestStatus(true, homePage);
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/logon", e);
			}
			return new RequestStatus(false, e.getMessage());
		} finally {
			permissionService.clearUserContext();
		}
	}
}
