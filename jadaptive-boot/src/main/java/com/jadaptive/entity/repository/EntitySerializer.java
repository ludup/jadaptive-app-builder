package com.jadaptive.entity.repository;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class EntitySerializer extends StdSerializer<EntityImpl> {

	private static final long serialVersionUID = 5624312163275460262L;

	public EntitySerializer() {
		super(EntityImpl.class);
	}

	@Override
	public void serialize(EntityImpl value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		
		EntityTemplate template = null;
		
		gen.writeStartObject();
		
		gen.writeStringField("uuid", value.getUuid());
		gen.writeObjectField("template", template);
		
		for(FieldCategory cat : template.getCategories()) {
			gen.writeObjectFieldStart(cat.getResourceKey());
			
			for(FieldTemplate t : cat.getTemplates()) {
				switch(t.getFieldType()) {
				case BOOLEAN:
					gen.writeBooleanField(t.getResourceKey(), Boolean.parseBoolean(value.getValue(t)));
					break;
				case TEXT:
				case TEXT_AREA:
					gen.writeStringField(t.getResourceKey(), value.getValue(t));
					break;
				case DECIMAL:
					gen.writeNumberField(t.getResourceKey(), Double.parseDouble(value.getValue(t)));
					break;
				case NUMBER:
					gen.writeNumberField(t.getResourceKey(), Long.parseLong(value.getValue(t)));
					break;
				}
			}
			
			gen.writeEndObject();
		}
		

		
		gen.writeEndObject();
		
	}
}
