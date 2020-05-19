package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;

public abstract class TemplatePage extends AuthenticatedView {

	@Autowired
	private EntityTemplateService templateService; 
	
	protected String resourceKey;
	protected EntityTemplate template; 
	protected boolean readOnly;
	

    public EntityTemplate getTemplate() {
    	return template;
    }

    protected void onCreated() throws FileNotFoundException {

	try {
	    template = templateService.get(resourceKey);
	} catch (EntityNotFoundException nse) {
	    throw new FileNotFoundException(String.format("No resource named %s", resourceKey));
	}
    }
    
	public boolean isReadOnly() {
		return readOnly;
	}
 
}
