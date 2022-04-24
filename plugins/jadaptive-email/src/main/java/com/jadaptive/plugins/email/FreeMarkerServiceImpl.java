package com.jadaptive.plugins.email;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

@Service
public class FreeMarkerServiceImpl implements FreeMarkerService {

	private Configuration cfg;
	private StringTemplateLoader stringLoader = new StringTemplateLoader();
	
	@SuppressWarnings("deprecation")
	@PostConstruct
	private void postConstruct() {
		
		cfg = new Configuration();
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
		cfg.setTemplateLoader(stringLoader);
	}
	
	@Override
	public Template createTemplate(String name, String templateSource, long lastModified) throws IOException {
		stringLoader.putTemplate(name, templateSource, lastModified);
		return cfg.getTemplate(name);
	}
}
