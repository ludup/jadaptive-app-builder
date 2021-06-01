package com.jadaptive.plugins.sshd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.stats.ResourceService;

@Service
public class SSHInterfaceServiceImpl implements SSHInterfaceService, ResourceService {

	@Autowired
	
	private TenantAwareObjectDatabase<SSHInterface> repository;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public SSHInterface getObjectByUUID(String uuid) {
		permissionService.assertRead(SSHInterface.RESOURCE_KEY);
		return repository.get(uuid, SSHInterface.class);
	}

	@Override
	public String saveOrUpdate(SSHInterface obj) {

		permissionService.assertReadWrite(SSHInterface.RESOURCE_KEY);
		
		repository.saveOrUpdate(obj);
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
