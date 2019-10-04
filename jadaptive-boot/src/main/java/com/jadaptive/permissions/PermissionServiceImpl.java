package com.jadaptive.permissions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.role.RoleService;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantService;

@Service
public class PermissionServiceImpl extends AbstractLoggingServiceImpl implements PermissionService {

	public static final String READ = "read";
	public static final String READ_WRITE = "readWrite";

	Set<String> allPermissions = new HashSet<>();
	
	@Autowired
	TenantService tenantService; 
	
	@Autowired
	RoleService roleService; 
	
	Map<Tenant,Set<String>> tenantPermissions = new HashMap<>();
	
	@Override
	public void registerStandardPermissions(String resourceKey) {
		
		registerPermission(getReadPermission(resourceKey));
		registerPermission(getReadWritePermission(resourceKey));
	}
	
	private synchronized void registerPermission(String permission) {
		
		Tenant tenant = tenantService.getCurrentTenant();	
		
		if(log.isInfoEnabled()) {
			log.info("Registering permission {}", permission);
		}

		Set<String> allPermissions = tenantPermissions.get(tenant);
		if(Objects.isNull(allPermissions)) {
			allPermissions = new HashSet<>();
			tenantPermissions.put(tenant, allPermissions);
		}
		
		allPermissions.add(permission);
	}
	
	@Override
	public void registerCustomPermission(String resourceKey, String permission) {
		
		String customPermission = String.format("%s.%s", resourceKey, permission);
		
		registerPermission(customPermission);
	}
	
	@Override
	public Collection<String> getAllPermissions() {
		return Collections.unmodifiableCollection(allPermissions);
	}
	
	@Override
	public void assertRead(String resourceKey) throws PermissionDeniedException {
		assertAnyPermission(resourceKey, getReadPermission(resourceKey), getReadWritePermission(resourceKey));
	}
	
	private String getReadWritePermission(String resourceKey) {
		return String.format("%s.%s", resourceKey, READ_WRITE);
	}

	private String getReadPermission(String resourceKey) {
		return String.format("%s.%s", resourceKey, READ);
	}

	@Override 
	public void assertReadWrite(String resourceKey) throws PermissionDeniedException {
		assertAnyPermission(resourceKey, getReadWritePermission(resourceKey));
	}
	
	@Override
	public void assertAnyPermission(String resourceKey, String... permissions) throws PermissionDeniedException {
		
	}
	
	@Override
	public void assertPermission(String resourceKey, String permission) throws PermissionDeniedException {
		
		
	}

}
