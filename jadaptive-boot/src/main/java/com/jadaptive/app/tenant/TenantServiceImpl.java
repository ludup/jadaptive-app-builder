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
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.templates.JsonTemplateEnabledService;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.tenant.TenantController;
import com.jadaptive.api.tenant.TenantRepository;
import com.jadaptive.api.tenant.TenantService;

@Service
public class TenantServiceImpl implements TenantService, JsonTemplateEnabledService<Tenant>, UUIDObjectService<Tenant> {

	final static Logger log = LoggerFactory.getLogger(TenantServiceImpl.class);
	
	ThreadLocal<Stack<Tenant>> currentTenant = new ThreadLocal<>();
	
	final public static String SYSTEM_TENANT_UUID = "cb3129ea-b8b1-48a4-85de-8443945d95e3";
	
	final public static String TENANT_RESOURCE_KEY = "tenant";
	
	@Autowired
	private TenantRepository repository; 
	
	@Autowired
	private TemplateVersionService templateService;
	
	@Autowired
	private PermissionService permissionService; 

	@Autowired
	private ApplicationService applicationService; 

	@Autowired
	private SingletonObjectDatabase<SystemConfiguration> systemConfig;
	Tenant systemTenant;
	
	Map<String,Tenant> tenantsByDomain = new HashMap<>();
	Map<String,Tenant> tenantsByUUID = new HashMap<>();
	
	boolean setupMode = false;
	boolean ready = false;
	TenantController controller = null;
	
	@EventListener
	public void onApplicationStartup(ApplicationReadyEvent evt) {
		
		permissionService.setupSystemContext();
		
		try {			
			boolean newSchema = repository.isEmpty() || Boolean.getBoolean("jadaptive.runFresh");
			if(newSchema) {
				repository.newSchema();
				systemTenant = createTenant(SYSTEM_UUID, "System", "localhost", true);
				setupMode = true;
			} else {
				systemTenant = repository.getSystemTenant();
			}
			
			templateService.registerAnnotatedTemplates();
			
			initialiseTenant(systemTenant, newSchema);
			
			for(Tenant tenant : allObjects()) {
				if(!tenant.isSystem()) {
					setCurrentTenant(tenant);
					try {
						templateService.registerTenantIndexes();
						initialiseTenant(tenant, false);
					} finally {
						clearCurrentTenant();
					}
				}
			}	
			
			this.ready = true;
			
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
	public void setTenantController(TenantController controller) {
		this.controller = controller;
	}
	
	@Override
	public boolean isReady() {
		return ready;
	}
	
	@Override
	public boolean hasCurrentTenant() {
		return currentTenant.get()!=null;
	}
	
	@Override
	public boolean isSetupMode() {
		SystemConfiguration cfg = systemConfig.getObject(SystemConfiguration.class);
		return !cfg.getSetupComplete().booleanValue();
	}
	
	@Override
	public void assertManageTenant() {
		permissionService.assertReadWrite(TENANT_RESOURCE_KEY);
	}
	
	@Override
	public Iterable<Tenant> allObjects()  {
		try {
			return repository.listTenants();
		} catch (RepositoryException | ObjectException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Tenant initialiseTenant(Tenant tenant, boolean newSchema) {
		
		setCurrentTenant(tenant);
		
		tenantsByDomain.put(tenant.getDomain(), tenant);
		tenantsByUUID.put(tenant.getUuid(), tenant);
		
		for(String domain : tenant.getAlternativeDomains()) {
			tenantsByDomain.put(domain, tenant);
		}
		
		try {
			
			Collection<JsonTemplateEnabledService> templateServices
				= ApplicationServiceImpl.getInstance().getBeans(
						JsonTemplateEnabledService.class);
			
			List<TenantAware> awareServices = new ArrayList<>(applicationService.getBeans(TenantAware.class));
			Collections.sort(awareServices, new Comparator<TenantAware>() {

				@Override
				public int compare(TenantAware o1, TenantAware o2) {
					return o1.getOrder().compareTo(o2.getOrder());
				}
				
			});
			
			for(TenantAware aware : awareServices) {
				if(tenant.isSystem()) {
					aware.initializeSystem(newSchema);
				} else {
					aware.initializeTenant(tenant, newSchema);
				}
			}

			List<JsonTemplateEnabledService> ordered = new ArrayList<JsonTemplateEnabledService>(templateServices);
			
			Collections.<JsonTemplateEnabledService>sort(ordered, new  Comparator<JsonTemplateEnabledService>() {
				@Override
				public int compare(JsonTemplateEnabledService o1, JsonTemplateEnabledService o2) {
					return o1.getTemplateOrder().compareTo(o2.getTemplateOrder());
				}
			});
			
			
			for(JsonTemplateEnabledService<?> repository : ordered) {
				if(tenant.isSystem() || !repository.isSystemOnly()) { 
					templateService.processTemplates(tenant, repository);		
				}
			}
		} finally {
			clearCurrentTenant();
		}
		
		return tenant;
		
	}
	
	@Override
	public boolean supportsMultipleTenancy() {
		if(Objects.nonNull(controller)) {
			return controller.supportsMultipleTenancy();
		}
		return false;
	}
	
	public Tenant getSystemTenant() throws RepositoryException, ObjectException {
		return systemTenant;
	}

	@Override
	public Tenant createTenant(String name, String ownerName, String ownerEmail, String primaryDomain, boolean system, String... additionalDomains) throws RepositoryException, ObjectException {
		return createTenant(UUID.randomUUID().toString(), name, ownerName, ownerEmail, primaryDomain, system, additionalDomains);
	}
	
	@Override
	public Tenant createTenant(String uuid, String name, String primaryDomain, boolean system) {
		return createTenant(uuid, name, "", "", primaryDomain, system);
	}

	@Override
	public Tenant createTenant(String uuid, String name, String ownerName, String ownerEmail, String primaryDomain, boolean system, String... additionalDomains) throws RepositoryException, ObjectException {
		
		if(Objects.nonNull(controller)) {
			if(!controller.supportsMultipleTenancy()) {
				throw new RepositoryException("Multiple tenancy is not enabled!");
			}
 		}
		
		if(tenantsByDomain.containsKey(primaryDomain)) {
			throw new ObjectException(String.format("%s is already used by another tenant", primaryDomain));
		}
		
		for(String domain : additionalDomains) {
			if(tenantsByDomain.containsKey(domain)) {
				throw new ObjectException(String.format("%s is already used by another tenant", primaryDomain));
			}
		}
		
		Tenant tenant = new Tenant(uuid, name, primaryDomain);
		tenant.setSystem(system);
		tenant.getAlternativeDomains().addAll(Arrays.asList(additionalDomains));
		tenant.setOwnerEmail(ownerEmail);
		tenant.setOwnerName(ownerName);
		
		try {
			repository.saveTenant(tenant);
			return tenant;
		} catch (RepositoryException | ObjectException e) {
			throw e;
		}
		
	}

	@Override
	public Tenant getCurrentTenant() throws RepositoryException, ObjectException {
		Stack<Tenant> tenant = currentTenant.get();
		if(Objects.isNull(tenant) || tenant.isEmpty()) {
			return getSystemTenant();
		}
		return tenant.peek();
	}
	
	@Override
	public void setCurrentTenant(Tenant tenant) {
		Stack<Tenant> tenants = currentTenant.get();
		if(Objects.isNull(tenants)) {
			currentTenant.set(new Stack<>());
			tenants = currentTenant.get();
		}
		currentTenant.get().push(tenant);
	}
	
	@Override
	public void clearCurrentTenant() {
		Stack<Tenant> tenants = currentTenant.get();
		if(Objects.isNull(tenants) || tenants.isEmpty()) {
			return;
		}
		tenants.pop();
	}

	@Override
	public Integer getTemplateOrder() {
		return Integer.MIN_VALUE;
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
			throws RepositoryException, ObjectException {

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
			throw new ObjectNotFoundException(String.format("Tenant %s not found", name));
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
			throw new ObjectNotFoundException(name + " not found!");
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
			throw new ObjectNotFoundException("Tenant does not exist for UUID");
		}
		return tenant;
	}

	@Override
	public Tenant getObjectByUUID(String uuid) {
		return getTenantByUUID(uuid);
	}

	@Override
	public String saveOrUpdate(Tenant object) {
		repository.saveTenant(object);
		
		setCurrentTenant(object);
		
		try {
			if(!tenantsByUUID.containsKey(object.getUuid())) {
				templateService.registerTenantIndexes();
				initialiseTenant(object, true);
			}
			return object.getUuid();
		} finally {
			clearCurrentTenant();
		}
	}

	@Override
	public void deleteObject(Tenant object) {
		assertManageTenant();
		repository.deleteTenant(object);
	}

	@Override
	public void completeSetup() {
		SystemConfiguration cfg = systemConfig.getObject(SystemConfiguration.class);
		cfg.setSetupComplete(true);
		systemConfig.saveObject(cfg);
	}
	
	@Override
	public void deleteObjectByUUID(String uuid) {
		deleteObject(getObjectByUUID(uuid));
	}
	
	@Override
	public void executeAs(Tenant tenant, Runnable r) {
		setCurrentTenant(tenant);
		try {
			r.run();
		} finally {
			clearCurrentTenant();
		}
	}
	
	@Override
	public <T> T executeAs(Tenant tenant, Callable<T> r) {
		setCurrentTenant(tenant);
		try {
			return r.call();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			clearCurrentTenant();
		}
	}

	@Override
	public boolean isSystemTenant() {
		return getCurrentTenant().isSystem();
	}

	@Override
	public <T> T asSystem(Callable<T> r) {
		return executeAs(getSystemTenant(), r);
	}

	@Override
	public void asSystem(Runnable r) {
		executeAs(getSystemTenant(), r);
	}
	
	
}
