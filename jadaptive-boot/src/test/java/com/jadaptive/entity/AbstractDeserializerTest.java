package com.jadaptive.entity;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.app.ApplicationServiceImpl;

public class AbstractDeserializerTest {

	protected ObjectMapper getMapper(Map<String, EntityTemplate> templates) {
		
		ApplicationServiceImpl.getInstance().registerTestingBean(EntityTemplateService.class, 
				new MockEntityTemplateService(templates));

		return new ObjectMapper();
	}
	
}
