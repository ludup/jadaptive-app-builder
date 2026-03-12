package com.jadaptive.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;
import com.sshtools.gardensched.ThrowingRunnable;

/**
 * Wraps a {@link Runnable} task and runs it in the context of a tenant and
 * optionally a user. This is used by the {@link TenantTask} interface to run
 * tasks in the context of a tenant, and optionally a user. This is useful for
 * running tasks that need to access tenant-specific data or perform actions on
 * behalf of a user.
 * <p>
 * Note, should not be used as a distributed task, use {@link TenantJobRunner} instead.
 */
public class LocalTenantJobRunner extends AbstractTenantJobRunner<Runnable> implements ThrowingRunnable  {

	static Logger log = LoggerFactory.getLogger(LocalTenantJobRunner.class);
	
	public LocalTenantJobRunner() {
	}
	
	public LocalTenantJobRunner(Tenant tenant, Runnable task) {
		super(tenant, task);
	}
	
	public LocalTenantJobRunner(Tenant tenant, Runnable task, User user) {
		super(tenant, task, user);
	}
	
	@Override
	public void execute() throws Exception {
		ApplicationServiceImpl.getInstance().autowire(task);
		tenantService.runAsTenant(tenantService.getTenantByUUID(tenantUUID), this::runAsUser);
	}
	
	public String getTenantUUID() {
		return tenantUUID;
	}


}
