package com.jadaptive.app.tenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.templates.TemplateEnabledService;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.tenant.TenantRepository;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.BuiltinUserDatabase;
import com.jadaptive.api.user.User;
import com.jadaptive.app.ApplicationServiceImpl;

@Service
public class TenantServiceImpl implements TenantService, TemplateEnabledService<Tenant> {

	ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();
	
	final public static String SYSTEM_TENANT_UUID = "cb3129ea-b8b1-48a4-85de-8443945d95e3";
	
	final public static String TENANT_RESOURCE_KEY = "tenant";
	
	@Autowired
	private TenantRepository repository; 
	
	@Autowired
	private TemplateVersionService templateService;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private BuiltinUserDatabase userService;
	
	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	Tenant systemTenant;
	
	Map<String,Tenant> tenantsByDomain = new HashMap<>();
	Map<String,Tenant> tenantsByUUID = new HashMap<>();
	
	@EventListener
	public void onApplicationStartup(ApplicationReadyEvent evt) {
		
		permissionService.setupSystemContext();
		
		try {
			boolean newSchema = repository.isEmpty() || Boolean.getBoolean("jadaptive.runFresh");
			if(newSchema) {
				repository.newSchema();
				createTenant(SYSTEM_UUID, "System", "localhost", true);
				
				setCurrentTenant(getSystemTenant());
				
				try {
					User user = userService.createUser("admin", "Administrator", "", "admin".toCharArray(), true);
					roleService.assignRole(roleService.getAdministrationRole(), user);
				} finally {
					
					clearCurrentTenant();
				}
			} else {
				
				initialiseTenant(getSystemTenant(), false);
				
				for(Tenant tenant : listTenants()) {
					if(!tenant.getSystem()) {
						initialiseTenant(tenant, false);
					}
				}	
			}
			
			
			permissionService.registerStandardPermissions(TENANT_RESOURCE_KEY);
			
            List<StartupAware> startups = new ArrayList<>(applicationService.getBeans(StartupAware.class));
            Collections.<StartupAware>sort(startups, new Comparator<StartupAware>() {

				@Override
				public int compare(StartupAware o1, StartupAware o2) {
					return o2.getStartupPosition().compareTo(o1.getStartupPosition());
				}
			});
            
			for(StartupAware startup : startups) {
				startup.onApplicationStartup();
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
	private Tenant initialiseTenant(Tenant tenant, boolean newSchema) {
		
		setCurrentTenant(tenant);
		
		tenantsByDomain.put(tenant.getDomain(), tenant);
		tenantsByUUID.put(tenant.getUuid(), tenant);
		
		for(String domain : tenant.getAlternativeDomains()) {
			tenantsByDomain.put(domain, tenant);
		}
		
		try {
			
			templateService.registerAnnotatedTemplates();
			
			Collection<TemplateEnabledService> templateServices
				= ApplicationServiceImpl.getInstance().getBeans(
						TemplateEnabledService.class);
			
			List<TemplateEnabledService> ordered = new ArrayList<TemplateEnabledService>(templateServices);
			
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
			
			for(TenantAware aware : applicationService.getBeans(TenantAware.class)) {
				if(tenant.getSystem()) {
					aware.initializeSystem(newSchema);
				} else {
					aware.initializeTenant(tenant, newSchema);
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
	public Tenant createTenant(String name, String domain, String... additionalDomains) throws RepositoryException, EntityException {
		return createTenant(UUID.randomUUID().toString(), name, domain, additionalDomains);
	}
	
	@Override
	public Tenant createTenant(String uuid, String name, String primaryDomain, String... additionalDomains) throws RepositoryException, EntityException {
		return createTenant(uuid, name, primaryDomain, false);
	}
	
	@Override
	public Tenant createTenant(String uuid, String name, String primaryDomain, boolean system, String... additionalDomains) throws RepositoryException, EntityException {
		
		if(tenantsByDomain.containsKey(primaryDomain)) {
			throw new EntityException(String.format("%s is already used by another tenant", primaryDomain));
		}
		
		for(String domain : additionalDomains) {
			if(tenantsByDomain.containsKey(domain)) {
				throw new EntityException(String.format("%s is already used by another tenant", primaryDomain));
			}
		}
		
		Tenant tenant = new Tenant(uuid, name, primaryDomain);
		tenant.setSystem(system);
		tenant.getAlternativeDomains().addAll(Arrays.asList(additionalDomains));
		
		try {
			repository.saveTenant(tenant);
			initialiseTenant(tenant, true);
			return tenant;
		} catch (RepositoryException | EntityException e) {
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
		Tenant tenant = tenantsByDomain.get(name);
		if(Objects.isNull(tenant)) {
			throw new EntityNotFoundException(String.format("Tenant %s not found", name));
		}
		return tenant;
	}
	
	@Override
	public Tenant getTenantByDomainOrDefault(String name) {
		Tenant tenant = tenantsByDomain.get(name);
		if(Objects.isNull(tenant)) {
			return getSystemTenant();
		}
		return tenant;
	}
	
	@Override
	public Tenant getTenantByName(String name) {
		Tenant tenant = repository.getTenantByName(name);
		if(Objects.isNull(tenant)) {
			throw new EntityNotFoundException(name + " not found!");
		}
		return tenant;
	}

	@Override
	public Tenant resolveTenantName(String username) {
		
		if(username.contains("@")) {
			String name = StringUtils.substringAfter(username, "@");
			Tenant tenant = tenantsByDomain.get(name);
			if(tenant!=null) {
				return tenant;
			}
		}
		
		if(username.contains("\\")) {
			String name = StringUtils.substringBefore(username, "\\");
			Tenant tenant = tenantsByDomain.get(name);
			if(tenant!=null) {
				return tenant;
			}
		}
		
		if(username.contains("/")) {
			String name = StringUtils.substringBefore(username, "/");
			Tenant tenant = tenantsByDomain.get(name);
			if(tenant!=null) {
				return tenant;
			}
		}
		return getSystemTenant();
	}

	@Override
	public Tenant getTenantByUUID(String uuid) {
		Tenant tenant = tenantsByUUID.get(uuid);
		if(Objects.isNull(tenant)) {
			return getSystemTenant();
		}
		return tenant;
	}
}
