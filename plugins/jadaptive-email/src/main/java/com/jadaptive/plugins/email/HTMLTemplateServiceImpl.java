package com.jadaptive.plugins.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.TenantAwareObjectDatabase;

@Service
public class HTMLTemplateServiceImpl implements HTMLTemplateService {

	@Autowired
	private TenantAwareObjectDatabase<HTMLTemplate> templateDatbase;
	
	@Override
	public Iterable<HTMLTemplate> allTemplates() {
		return templateDatbase.list(HTMLTemplate.class);
	}
	
	@Override 
	public void saveTemplate(HTMLTemplate template) {
		templateDatbase.saveOrUpdate(template);
	}
	
	
}
