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

public abstract class AbstractTenantJobRunner<T extends Runnable> implements Runnable {

	static Logger log = LoggerFactory.getLogger(AbstractTenantJobRunner.class);
	
	@Autowired
	protected TenantService tenantService; 

	@Autowired
	protected UserService userService;
	
	@Autowired
	protected PermissionService permissionService; 
	
	protected T task;
	protected String tenantUUID;
	protected String user = null;
	
	public AbstractTenantJobRunner() {
	}
	
	public AbstractTenantJobRunner(Tenant tenant, T task) {
		this.tenantUUID = tenant.getUuid();
		this.task = task;
	}
	
	public AbstractTenantJobRunner(Tenant tenant, T task, User user) {
		this.tenantUUID = tenant.getUuid();
		this.task = task;
		this.user = user.getUuid();
	}
	
	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public T getTask() {
		return task;
	}

	public void setTask(T task) {
		this.task = task;
	}

	public void setTenantUUID(String tenantUUID) {
		this.tenantUUID = tenantUUID;
	}

	protected void runAsUser() {
		permissionService.as(user == null ? permissionService.getSystemUser() : userService.getUserByUUID(user), ()->{
			for(TaskRunnerContext ctx : ApplicationServiceImpl.getInstance().getBeans(TaskRunnerContext.class)) {
				ctx.setupContext();
			}
			task.run();
		});
	}
	
	public String getTenantUUID() {
		return tenantUUID;
	}


}
