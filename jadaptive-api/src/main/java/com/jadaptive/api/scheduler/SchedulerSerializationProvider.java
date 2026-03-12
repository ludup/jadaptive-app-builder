package com.jadaptive.api.scheduler;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;

public interface SchedulerSerializationProvider<T> {

	Class<T> getType();
	
	JsonSerializer<T> getSerializer();
	
	JsonDeserializer<T> getDeserializer();
}
