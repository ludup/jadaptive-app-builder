package com.jadaptive.app.session;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.TaskScope;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.utils.Utils;

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
				
				Date threshold = DateUtils.addMinutes(session.getSignedOut(), 5);
				if(threshold.before(Utils.now())) {
					sessionService.deleteSession(session);
				}
			}
		} finally {
			permissionService.clearUserContext();
		}
	}

	@Override
	public String cron() {
		return AT_MIDNIGHT;
	}
	
	@Override
	public TaskScope getScope() {
		return TaskScope.GLOBAL;
	}

}
