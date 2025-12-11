package com.jadaptive.app.tomcat;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.certificates.Keyring;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.ScheduledTaskConfig;
import com.jadaptive.api.scheduler.TenantTaskConfig;
import com.sshtools.gardensched.Affinity;
import com.sshtools.gardensched.TaskConfig;

@Component
@TaskConfig(key = "reloadCertificatesJob", bundle = "default",  affinity = Affinity.ALL)
//@ScheduledTaskConfig(value = "0 1 * * * *", systemOnly = true)
@ScheduledTaskConfig(value = ScheduledTask.EVERY_MINUTE, systemOnly = true)
@TenantTaskConfig(allowTenantRunNow = true, allowRunNow = true)
public class ReloadCertificatesJob implements ScheduledTask {

	private static final long serialVersionUID = 7265958285265803719L;

	private static Logger log = LoggerFactory.getLogger(ReloadCertificatesJob.class);
	
	@Autowired
	private Keyring keyring;
	
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

}
