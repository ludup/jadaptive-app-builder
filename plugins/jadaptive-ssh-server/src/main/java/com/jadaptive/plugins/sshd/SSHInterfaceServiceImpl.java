package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.stats.ResourceService;

@Service
public class SSHInterfaceServiceImpl extends AbstractUUIDObjectServceImpl<SSHInterface> implements SSHInterfaceService, ResourceService {

	static Logger log = LoggerFactory.getLogger(SSHInterfaceServiceImpl.class);
	
	@Autowired
	private TenantAwareObjectDatabase<SSHInterface> repository;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	SSHDService sshdService; 

	@Override
	protected void beforeSave(SSHInterface obj) {

		permissionService.assertWrite(SSHInterface.RESOURCE_KEY);
		
		try {
			SSHInterface prev = getObjectByUUID(obj.getUuid());
			try {
				sshdService.removeInterface(prev);
			} catch(UnknownHostException e) {
				throw new ObjectException(String.format("Invalid interface address %s", obj.getAddressToBind()));
			}
		} catch(ObjectNotFoundException e) {
			// Continue;
		}
		
	}
	
	@Override
	protected void afterSave(SSHInterface obj) {
		
		boolean success = false;
		int i = 0;
		do {
			try {
				sshdService.addInterface(obj);
				success = true;
				if(log.isInfoEnabled()) {
					log.info("Interface {} is back up.", obj.getInterface());
				}
				continue;
			} catch (IOException e) {
				i++;
				if(i >= 3) {
					throw new ObjectException("Interface saved it could not be restarted!", e);
				}
				if(log.isInfoEnabled()) {
					log.info("Interface {} did not come back up. Waiting 5 seconds before trying again.", obj.getAddressToBind());
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
				}
				continue;
			}
		} while(!success);
		
		try {
			if(!sshdService.isRunning()) {
				sshdService.start(true);
			}
		} catch(IOException e) {
			throw new ObjectException("The SSH service failed to start", e);
		}
	}

	@Override
	public long getTotalResources() {
		return repository.count(SSHInterface.class);
	}

	@Override
	public String getResourceKey() {
		return "sshInterface";
	}

	@Override
	protected Class<SSHInterface> getResourceClass() {
		return SSHInterface.class;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
