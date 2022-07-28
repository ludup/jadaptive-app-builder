package com.jadaptive.app.session;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;

@Extension
public class SessionCleanupTask implements ScheduledTask {

	@Autowired
	private SessionService sessionService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public void run() {
		
		permissionService.setupSystemContext();
		
		try {
			for(Session session : sessionService.inactiveSessions()) {
				sessionService.deleteSession(session);
			}
		} finally {
			permissionService.clearUserContext();
		}
	}

	@Override
	public String cron() {
		return AT_MIDNIGHT;
	}

}
