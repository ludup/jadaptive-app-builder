package com.jadaptive.app.json;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ObjectMapperHolder {

	static ObjectMapper objectMapper = new ObjectMapper();
	
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

}
