package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AuthenticatedPage;

public abstract class TemplatePage extends AuthenticatedPage {

	@Autowired
	protected TemplateService templateService;
	
	@Autowired
	private TenantService tenantService; 
	
	protected String resourceKey;
	
	protected ObjectTemplate template;
	protected Class<?> templateClazz;

	public ObjectTemplate getTemplate() {
		return template;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}

	public void onCreate() throws FileNotFoundException {
		
		
		try {
			template = templateService.get(resourceKey);
			templateClazz = templateService.getTemplateClass(resourceKey);
			
			if(!tenantService.getCurrentTenant().isSystem() && template.isSystem()) {
				throw new FileNotFoundException(String.format("%s not found", resourceKey));
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw e;
		} catch (ObjectException e) {
			e.printStackTrace();
			throw new FileNotFoundException(String.format("%s not found", resourceKey));
		}
	
	}
	
	public abstract FieldView getScope();
	
}
