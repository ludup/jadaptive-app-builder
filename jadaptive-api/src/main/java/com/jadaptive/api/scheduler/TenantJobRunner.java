package com.jadaptive.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;
import com.sshtools.gardensched.SerializableJob;

@SuppressWarnings("serial")
public class TenantJobRunner extends AbstractTenantJobRunner<TenantTask> implements SerializableJob {

	static Logger log = LoggerFactory.getLogger(TenantJobRunner.class);
	
	
	public TenantJobRunner() {
		super();
	}
	
	public TenantJobRunner(Tenant tenant, TenantTask task) {
		super(tenant, task);
	}
	
	public TenantJobRunner(Tenant tenant, TenantTask task, User user) {
		super(tenant, task, user);
	}
	
	@Override
	public void execute() throws Exception {
		ApplicationServiceImpl.getInstance().autowire(task);
		tenantService.runAsTenant(tenantService.getTenantByUUID(tenantUUID), () -> {
			if(task.isLogging() && log.isInfoEnabled()) {
				log.info("Running {} on tenant {}", task.getClass().getSimpleName(), tenantService.getCurrentTenant().getName());
			}
			runAsUser();
		});
	}
	
	public String getTenantUUID() {
		return tenantUUID;
	}


}
