package com.jadaptive.tenant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.jadaptive.app.ApplicationServiceImpl;
import com.jadaptive.entity.EntityException;
import com.jadaptive.permissions.PermissionService;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.repository.TransactionAdapter;
import com.jadaptive.templates.TemplateEnabledService;
import com.jadaptive.templates.TemplateVersionService;

@Service
public class TenantServiceImpl implements TenantService, TemplateEnabledService<Tenant> {

	ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();
	
	final public static String SYSTEM_TENANT_UUID = "cb3129ea-b8b1-48a4-85de-8443945d95e3";
	
	final public static String TENANT_RESOURCE_KEY = "tenant";
	@Autowired
	TenantRepository repository; 
	
	@Autowired
	TemplateVersionService templateService;
	
	@Autowired
	PermissionService permissionService; 
	
	Tenant systemTenant;
	
	Map<String,Tenant> tenantsByHostname = new HashMap<>();
	
	@EventListener
	private void setup(ApplicationReadyEvent event) throws RepositoryException, EntityException {
		
		if(Boolean.getBoolean("jadaptive.runFresh")) {
			repository.newSchema();
		}
		
		permissionService.registerStandardPermissions(TENANT_RESOURCE_KEY);
		
		initialiseTenant(getSystemTenant());
		
		for(Tenant tenant : getTenants()) {
			if(!tenant.getSystem()) {
				initialiseTenant(tenant);
			}
		}
	}
	
	@Override
	public Collection<Tenant> getTenants()  {
		try {
			return repository.listTenants();
		} catch (RepositoryException | EntityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Tenant initialiseTenant(Tenant tenant) {
		
		setCurrentTenant(tenant);
		
		tenantsByHostname.put(tenant.getHostname(), tenant);
		
		try {
			Map<String,TemplateEnabledService> templateServices
				= ApplicationServiceImpl.getInstance().getContext().getBeansOfType(
						TemplateEnabledService.class);
			
			List<TemplateEnabledService> ordered = new ArrayList<TemplateEnabledService>(templateServices.values());
			
			Collections.<TemplateEnabledService>sort(ordered, new  Comparator<TemplateEnabledService>() {
				@Override
				public int compare(TemplateEnabledService o1, TemplateEnabledService o2) {
					return o1.getWeight().compareTo(o2.getWeight());
				}
			});
			
			
			for(TemplateEnabledService<?> repository : ordered) {
				if(tenant.getSystem() || !repository.isSystemOnly()) { 
					templateService.processTemplates(repository);		
				}
			}
	
		} finally {
			clearCurrentTenant();
		}
		
		return tenant;
		
	}
	
	public Tenant getSystemTenant() throws RepositoryException, EntityException {
		return repository.getSystemTenant();
	}
	
	@Override
	public Tenant createTenant(String name, String hostname) throws RepositoryException, EntityException {
		return initialiseTenant(createTenant(UUID.randomUUID().toString(), name, hostname));
	}
	
	public Tenant createTenant(String uuid, String name, String hostname) throws RepositoryException, EntityException {
		
		if(tenantsByHostname.containsKey(hostname)) {
			throw new EntityException(String.format("%s is already used by another tenant", hostname));
		}
		
		Tenant tenant = new Tenant(uuid, name, hostname);
		repository.saveTenant(tenant);
		return tenant;
		
	}

	@Override
	public Tenant getCurrentTenant() throws RepositoryException, EntityException {
		Tenant tenant = currentTenant.get();
		if(Objects.isNull(tenant)) {
			return getSystemTenant();
		}
		return tenant;
	}
	
	@Override
	public void setCurrentTenant(Tenant tenant) {
		currentTenant.set(tenant);
	}
	
	@Override
	public void clearCurrentTenant() {
		currentTenant.remove();
	}

	@Override
	public Integer getWeight() {
		return Integer.MIN_VALUE;
	}

	@Override
	public Tenant createEntity() {
		return new Tenant();
	}

	@Override
	public String getName() {
		return "Tenant";
	}

	@Override
	public String getResourceKey() {
		return "tenant";
	}

	@Override
	public Class<Tenant> getResourceClass() {
		return Tenant.class;
	}

	@Override
	public void saveTemplateObjects(List<Tenant> tenants, TransactionAdapter<Tenant>... ops)
			throws RepositoryException, EntityException {

		for(Tenant tenant : tenants) {
			repository.saveTenant(tenant);
		}
	}

	@Override
	public void onTemplatesComplete(String... resourceKeys) {
		
	}
	
	@Override
	public boolean isSystemOnly() {
		return true;
	}

	@Override
	public void setCurrentTenant(HttpServletRequest request) {
		Tenant tenant = tenantsByHostname.get(request.getServerName());
		if(Objects.isNull(tenant)) {
			tenant = tenantsByHostname.get(request.getHeader("Host"));
			if(Objects.isNull(tenant)) {
				tenant = getSystemTenant();
			}
		}
		setCurrentTenant(tenant);
	}
}
