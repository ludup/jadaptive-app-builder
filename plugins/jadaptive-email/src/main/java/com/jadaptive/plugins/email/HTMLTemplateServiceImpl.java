package com.jadaptive.plugins.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;

@Service
public class HTMLTemplateServiceImpl implements HTMLTemplateService {

	@Autowired
	private TenantAwareObjectDatabase<HTMLTemplate> templateDatbase;
	
	@Override
	public HTMLTemplate getTemplateByShortName(String shortName) {
		return templateDatbase.get(HTMLTemplate.class, SearchField.eq("shortName", shortName));
	}
	
	@Override
	public Iterable<HTMLTemplate> allTemplates() {
		return templateDatbase.list(HTMLTemplate.class);
	}
	
	@Override 
	public void saveTemplate(HTMLTemplate template) {
		templateDatbase.saveOrUpdate(template);
	}
	
	
}
