package com.jadaptive.entity;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;

public class AbstractDeserializerTest {

	protected ObjectMapper getMapper(Map<String, ObjectTemplate> templates) {
		
		ApplicationServiceImpl.getInstance().registerTestingBean(TemplateService.class, 
				new MockEntityTemplateService(templates));

		return new ObjectMapper();
	}
	
}
