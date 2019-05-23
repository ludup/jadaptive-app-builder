package com.jadaptive.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.jadaptive.AbstractLoggingServiceImpl;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.TenantService;

@Service
public class TemplateServiceImpl extends AbstractLoggingServiceImpl implements TemplateService  {
	
	@Autowired
	ApplicationContext appContext;
	
	@Autowired
	TemplateRepository templateRepository; 
	
	@Autowired
	TenantService tenantService; 
	
	@EventListener
	@SuppressWarnings("rawtypes")
	private void setup(ApplicationReadyEvent event) {
		
		Map<String,TemplateEnabledUUIDRepository> repositories
			= appContext.getBeansOfType(
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
		
	}

	@Override
	public Collection<Template> list() throws RepositoryException {
		return templateRepository.list(tenantService.getCurrentTenant());
	}
}
