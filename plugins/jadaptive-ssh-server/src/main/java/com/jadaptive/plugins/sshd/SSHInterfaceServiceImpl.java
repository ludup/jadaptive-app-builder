package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.stats.ResourceService;

@Service
public class SSHInterfaceServiceImpl implements SSHInterfaceService, ResourceService {

	@Autowired
	private TenantAwareObjectDatabase<SSHInterface> repository;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	SSHDService sshdService; 
	
	@Override
	public SSHInterface getObjectByUUID(String uuid) {
		permissionService.assertRead(SSHInterface.RESOURCE_KEY);
		return repository.get(uuid, SSHInterface.class);
	}

	@Override
	public String saveOrUpdate(SSHInterface obj) {

		permissionService.assertReadWrite(SSHInterface.RESOURCE_KEY);
		
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
		
		repository.saveOrUpdate(obj);
		
		
		try {
			sshdService.addInterface(obj);
		} catch (IOException e) {
			throw new ObjectException("Interface saved but server threw an error when it was started", e);
		}
		
		try {
			if(!sshdService.isRunning()) {
				sshdService.start(true);
			}
		} catch(IOException e) {
			throw new ObjectException("The SSH service failed to start", e);
		}
		
		return obj.getUuid();
	}

	@Override
	public void deleteObject(SSHInterface obj) {
		permissionService.assertReadWrite(SSHInterface.RESOURCE_KEY);
		repository.delete(obj);
	}

	@Override
	public Iterable<SSHInterface> allObjects() {
		return repository.list(SSHInterface.class);
	}
	
	@Override
	public long getTotalResources() {
		return repository.count(SSHInterface.class);
	}

	@Override
	public String getI18NKey() {
		return "sshInterface";
	}

}
