package com.jadaptive.app.scheduler;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;

public class TenantJobRunner implements Runnable {

	static Logger log = LoggerFactory.getLogger(TenantJobRunner.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private TaskScheduler taskScheduler;
	
	ScheduledTask task;
	ScheduledFuture<?> future;
	String tenantUUID;
	
	public TenantJobRunner(Tenant tenant) {
		this.tenantUUID = tenant.getUuid();
	}
	
	public void schedule(ScheduledTask task) {
		this.task = task;
		future = taskScheduler.schedule(this, new CronTrigger(task.cron()));
	}
	
	@Override
	public void run() {
		
		Tenant tenant = null;
		
		try {
			tenant = tenantService.getTenantByUUID(tenantUUID);
		} catch(ObjectNotFoundException e) {
			log.error("Tenant does not exist for UUID {}", tenantUUID);
			future.cancel(false);
			return;
		}
		
		tenantService.setCurrentTenant(tenant);
		
		try {
			task.run();
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

}
