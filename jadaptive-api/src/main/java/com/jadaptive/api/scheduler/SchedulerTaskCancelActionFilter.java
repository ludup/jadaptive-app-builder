package com.jadaptive.api.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.ActionFilter;
import com.jadaptive.api.tenant.TenantService;
import com.sshtools.gardensched.ClusterID;

public class SchedulerTaskCancelActionFilter implements ActionFilter {
	
	@Autowired
	private SchedulerService schedulerService;
	
	@Autowired
	private TenantService tenantService;

	@Override
	public boolean showAction(AbstractObject object) {
		var tsk = schedulerService.getObjectByUUID(object.getUuid());
		if(tsk != null) {
			var fut = schedulerService.future(ClusterID.parse(tsk.getId()));
			if(fut != null) {
				if(tenantService.isSystemTenant())
					return (Boolean)fut.attributes().getOrDefault(SchedulerTask.ALLOW_CANCEL, false);
				else
					return (Boolean)fut.attributes().getOrDefault(SchedulerTask.ALLOW_TENANT_CANCEL, false);
			}
		}
		return false;
	}

}
