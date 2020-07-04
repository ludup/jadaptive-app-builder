package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.FieldView;

public abstract class TemplatePage extends AuthenticatedView {

	@Autowired
	private TemplateService templateService; 
	
	protected String resourceKey;
	protected ObjectTemplate template; 

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
    
	public abstract FieldView getScope();
 
}
