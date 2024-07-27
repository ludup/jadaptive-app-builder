package com.jadaptive.plugins.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.permissions.PermissionService;

@Component
public class DashboardInitialiser implements StartupAware {

	public static final String DASHBOARD_PERMISSION = "dashboard.read";
	
	@Autowired
	private PermissionService permissionService;
	
	@Override
	public void onApplicationStartup() {
		permissionService.registerCustomPermission(DASHBOARD_PERMISSION);
	}
}
