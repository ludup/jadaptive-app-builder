package com.jadaptive.app.tenant;

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

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.templates.TemplateEnabledService;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.tenant.TenantRepository;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.tenant.events.TenantCreatedEvent;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.app.ApplicationServiceImpl;
import com.jadaptive.app.repository.RepositoryException;
import com.jadaptive.app.repository.TransactionAdapter;


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
	
	@Autowired
	EventService eventService; 
	
	@Autowired
	UserService userService;
	
	@Autowired
	RoleService roleService; 
	
	Tenant systemTenant;
	
	Map<String,Tenant> tenantsByHostname = new HashMap<>();
	
	
	@EventListener
	private void setup(ApplicationReadyEvent event) throws RepositoryException, EntityException {
		
		permissionService.setupSystemContext();
		
		try {
			if(repository.isEmpty() || Boolean.getBoolean("jadaptive.runFresh")) {
				repository.newSchema();
				createTenant(SYSTEM_UUID, "System", "localhost", true);
				
				setCurrentTenant(getSystemTenant());
				
				
				try {
					User user = userService.createUser("admin", "admin".toCharArray(), "Administrator");
					roleService.assignRole(roleService.getAdministrationRole(), user);
				} finally {
					
					clearCurrentTenant();
				}
			}
			
			permissionService.registerStandardPermissions(TENANT_RESOURCE_KEY);
			
			initialiseTenant(getSystemTenant());
			
			for(Tenant tenant : listTenants()) {
				if(!tenant.getSystem()) {
					initialiseTenant(tenant);
				}
			}
		} finally {
			permissionService.clearUserContext();
		}
	}
	
	@Override
	public void assertManageTenant() {
		permissionService.assertReadWrite(TENANT_RESOURCE_KEY);
	}
	
	@Override
	public Collection<Tenant> listTenants()  {
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
					templateService.processTemplates(tenant, repository);		
				}
			}
			
			for(TenantAware aware : ApplicationServiceImpl.getInstance().getContext().getBeansOfType(
						TenantAware.class).values()) {
				aware.initializeTenant(tenant);
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
		return createTenant(uuid, name, hostname, false);
	}
	
	public Tenant createTenant(String uuid, String name, String hostname, boolean system) throws RepositoryException, EntityException {
		
		if(tenantsByHostname.containsKey(hostname)) {
			throw new EntityException(String.format("%s is already used by another tenant", hostname));
		}
		
		Tenant tenant = new Tenant(uuid, name, hostname);
		tenant.setSystem(system);
		try {
			repository.saveTenant(tenant);
			eventService.publishEvent(new TenantCreatedEvent(this, tenant));
			return tenant;
		} catch (RepositoryException | EntityException e) {
			eventService.publishEvent(new TenantCreatedEvent(this, tenant, e));
			throw e;
		}
		
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
		return "Tenants";
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
	public void deleteTenant(Tenant tenant) {
		
		permissionService.assertReadWrite(TENANT_RESOURCE_KEY);
		repository.deleteTenant(tenant);
	}
	
	@Override
	public void saveTemplateObjects(List<Tenant> tenants, @SuppressWarnings("unchecked") TransactionAdapter<Tenant>... ops)
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
		setCurrentTenant(getTenantByDomainOrDefault(request.getServerName()));
	}
	
	@Override
	public void setCurrentTenant(String name) {
		setCurrentTenant(getTenantByDomainOrDefault(name));
	}

	@Override
	public String getTemplateFolder() {
		return "tenants";
	}

	@Override
	public Tenant getTenantByDomain(String name) {
		Tenant tenant = tenantsByHostname.get(name);
		if(Objects.isNull(tenant)) {
			throw new EntityNotFoundException(String.format("Tenant %s not found", name));
		}
		return tenant;
	}
	
	@Override
	public Tenant getTenantByDomainOrDefault(String name) {
		Tenant tenant = tenantsByHostname.get(name);
		if(Objects.isNull(tenant)) {
			return getSystemTenant();
		}
		return tenant;
	}
}
