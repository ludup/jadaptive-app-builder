package com.jadaptive.app.tomcat;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.certificates.Keyring;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.TaskScope;

@Component
public class ReloadCertificatesJob implements ScheduledTask {

	private static Logger log = LoggerFactory.getLogger(ReloadCertificatesJob.class);
	
	@Autowired
	Keyring keyring;
	
	@Override
	public void run() {
		try {
			if(log.isInfoEnabled()) {
				log.info("Reloading custom certificates");
			}
			keyring.reload();
		} catch (IOException e) {
			log.error("Failed to reload certificates", e);
		}
	}

	@Override
	public TaskScope getScope() {
		return TaskScope.NODE;
	}

	@Override
	public String cron() {
		return "0 1 * * * *";
	}
	
	

}
