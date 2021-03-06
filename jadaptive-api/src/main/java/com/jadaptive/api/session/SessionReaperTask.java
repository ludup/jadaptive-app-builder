package com.jadaptive.api.session;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.scheduler.ScheduledTask;

@Extension
public class SessionReaperTask implements ScheduledTask {

	@Autowired
	private SessionService sessionService; 
	
	@Override
	public void run() {
		
		for(Session session : sessionService.iterateSessions()) {
			sessionService.isLoggedOn(session, false);
		}
		
	}

	@Override
	public String cron() {
		return EVERY_MINUTE;
	}

}
