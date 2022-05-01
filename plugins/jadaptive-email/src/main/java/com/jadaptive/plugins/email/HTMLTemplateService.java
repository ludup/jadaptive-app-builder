package com.jadaptive.plugins.email;

public interface HTMLTemplateService {

	Iterable<HTMLTemplate> allTemplates();

	void saveTemplate(HTMLTemplate template);

}
