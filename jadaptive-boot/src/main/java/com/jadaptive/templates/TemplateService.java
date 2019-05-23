package com.jadaptive.templates;

import java.util.Collection;

import com.jadaptive.repository.RepositoryException;

public interface TemplateService {

	Collection<Template> list() throws RepositoryException;

}
