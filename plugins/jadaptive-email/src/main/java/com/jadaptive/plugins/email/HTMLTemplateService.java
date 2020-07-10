package com.jadaptive.plugins.email;

public interface HTMLTemplateService {

	HTMLTemplate getTemplateByShortName(String shortName);

	Iterable<HTMLTemplate> allTemplates();

	void saveTemplate(HTMLTemplate template);

}
