package com.jadaptive.tenant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.jadaptive.app.ApplicationServiceImpl;
import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.templates.TemplateEnabledUUIDRepository;

@Service
public class TenantServiceImpl implements TenantService {

	ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();
	
	final public static String SYSTEM_TENANT_UUID = "cb3129ea-b8b1-48a4-85de-8443945d95e3";
	
	@Autowired
	TenantRepository repository; 
	
	@EventListener
	private void setup(ApplicationReadyEvent event) throws RepositoryException {
		
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
			return repository.getTenants();
		} catch (RepositoryException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Tenant initialiseTenant(Tenant tenant) {
		
		setCurrentTenant(tenant);
		
		try {
			Map<String,TemplateEnabledUUIDRepository> repositories
				= ApplicationServiceImpl.getInstance().getContext().getBeansOfType(
						TemplateEnabledUUIDRepository.class);
			
			List<TemplateEnabledUUIDRepository> ordered = new ArrayList<TemplateEnabledUUIDRepository>(repositories.values());
			
			Collections.<TemplateEnabledUUIDRepository>sort(ordered, new  Comparator<TemplateEnabledUUIDRepository>() {
				@Override
				public int compare(TemplateEnabledUUIDRepository o1, TemplateEnabledUUIDRepository o2) {
					return o1.getWeight().compareTo(o2.getWeight());
				}
			});
			
			for(TemplateEnabledUUIDRepository repository : ordered) {
				repository.processTemplates();
			}
	
		} finally {
			clearCurrentTenant();
		}
		
		return tenant;
		
	}
	
	private Tenant getSystemTenant() throws RepositoryException {
	
		try {
			return repository.get(SYSTEM_TENANT_UUID);
		} catch (EntityNotFoundException e) {
			return createTenant(SYSTEM_TENANT_UUID, "System", "localhost");
		}
	}
	
	@Override
	public Tenant createTenant(String name, String hostname) throws RepositoryException {
		return initialiseTenant(createTenant(UUID.randomUUID().toString(), name, hostname));
	}
	
	@SuppressWarnings("unchecked")
	public Tenant createTenant(String uuid, String name, String hostname) throws RepositoryException {
		
		Tenant tenant = new Tenant(uuid, name, hostname);
		repository.save(tenant);
		return tenant;
		
	}

	@Override
	public Tenant getCurrentTenant() throws RepositoryException {
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
}
