package com.jadaptive.app.auth.quota;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.session.PluginInterceptor;
import com.jadaptive.app.json.ResponseHelper;

@Extension
public class QuotaInterceptor implements PluginInterceptor {

	@Autowired
	private SecurityPropertyService securityService;
	
	@Autowired
	private QuotaService quotaService; 
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
		
		if(Boolean.parseBoolean(properties.getProperty("quota.enabled", "false"))) {
			try {
				String group = properties.getProperty("quota.group", "defaultGroup");
				String key = request.getRemoteAddr();
				long quota = Long.parseLong(properties.getProperty("quota.threshold", "10000"));
				long period = Long.parseLong(properties.getProperty("quota.period", "86400"));
				
				quotaService.incrementQuota(group, key, quota, period);
			} catch(AccessDeniedException e) {
				ResponseHelper.send403Forbidden(request, response);
				return true;
			}
		}
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	}
}
