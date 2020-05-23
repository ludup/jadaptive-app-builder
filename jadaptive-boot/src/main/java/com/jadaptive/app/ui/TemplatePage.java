package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;

public abstract class TemplatePage extends AuthenticatedView {

	@Autowired
	private TemplateService templateService; 
	
	protected String resourceKey;
	protected ObjectTemplate template; 
	protected boolean readOnly;
	

    public ObjectTemplate getTemplate() {
    	return template;
    }

    protected void onCreated() throws FileNotFoundException {

	try {
	    template = templateService.get(resourceKey);
	} catch (ObjectNotFoundException nse) {
	    throw new FileNotFoundException(String.format("No resource named %s", resourceKey));
	}
    }
    
	public boolean isReadOnly() {
		return readOnly;
	}
 
}
