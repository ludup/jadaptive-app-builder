package com.jadaptive.templates;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.repository.RepositoryException;

@Service
public class TemplateServiceImpl extends AbstractLoggingServiceImpl implements TemplateService  {
	
	@Autowired
	TemplateRepository templateRepository; 

	@Override
	public Collection<Template> list() throws RepositoryException {
		return templateRepository.list();
	}
}
