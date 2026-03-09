package com.jadaptive.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.jobs.TaskRunnerContext;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.gardensched.SerializableJob;

@SuppressWarnings("serial")
public class TenantJobRunner implements SerializableJob {

	static Logger log = LoggerFactory.getLogger(TenantJobRunner.class);
	
	@Autowired
	private TenantService tenantService; 

	@Autowired
	private UserService userService;
	
	@Autowired
	private PermissionService permissionService; 
	
	private TenantTask task;
	private String tenantUUID;
	private String user = null;
	
	public TenantJobRunner() {
		
	}
	
	public TenantJobRunner(Tenant tenant, TenantTask task) {
		this.tenantUUID = tenant.getUuid();
		this.task = task;
	}
	
	public TenantJobRunner(Tenant tenant, TenantTask task, User user) {
		this.tenantUUID = tenant.getUuid();
		this.task = task;
		this.user = user.getUuid();
	}
	
	public void setUser(String user) {
		this.user = user;
	}

	public TenantTask getTask() {
		return task;
	}

	public void setTask(TenantTask task) {
		this.task = task;
	}

	public String getUser() {
		return user;
	}

	public void setTenantUUID(String tenantUUID) {
		this.tenantUUID = tenantUUID;
	}

	@Override
	public void execute() throws Exception {

		ApplicationServiceImpl.getInstance().autowire(task);
		
		final Tenant tenant = tenantService.getTenantByUUID(tenantUUID);
		tenantService.setCurrentTenant(tenant);
		
		try {

			permissionService.as(user == null ? permissionService.getSystemUser() : userService.getUserByUUID(user), ()->{
				for(TaskRunnerContext ctx : ApplicationServiceImpl.getInstance().getBeans(TaskRunnerContext.class)) {
					ctx.setupContext();
				}
				
				if(task.isLogging() && log.isInfoEnabled()) {
					log.info("Running {} on tenant {}", task.getClass().getSimpleName(), tenant.getName());
				}
				task.run();
			});
		} finally {
			tenantService.clearCurrentTenant();
		}
		
		
	}
	
	public String getTenantUUID() {
		return tenantUUID;
	}


}
