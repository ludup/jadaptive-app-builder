package com.jadaptive.entity.template;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class FieldTemplateSerializer extends StdSerializer<FieldTemplate> {

	private static final long serialVersionUID = 5624312163275460262L;

	public FieldTemplateSerializer() {
		super(FieldTemplate.class);
	}

	@Override
	public void serialize(FieldTemplate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		
		gen.writeStartObject();
		
		gen.writeStringField("uuid", value.getUuid());
		gen.writeStringField("resourceKey", value.getResourceKey());
		gen.writeStringField("defaultValue", value.getDefaultValue());
		gen.writeStringField("description", value.getDescription());
		gen.writeStringField("fieldType", value.getFieldType().name());
		gen.writeBooleanField("hidden",  value.getHidden());
		gen.writeNumberField("weight", value.getWeight());
		
		for(FieldMetaValue v : value.getMetaValues()) {
			switch(v.getType()) {
			case NUMBER:
				gen.writeNumberField(v.getResourceKey(), Long.parseLong(v.getValue()));
				break;
			case BOOLEAN:
				gen.writeBooleanField(v.getResourceKey(), Boolean.valueOf(v.getValue()));
				break;
			case DECIMAL:
				gen.writeNumberField(v.getResourceKey(), Double.parseDouble(v.getValue()));
				break;
			default:
				gen.writeStringField(v.getResourceKey(), v.getValue());
				break;
			}
		}
		
		gen.writeEndObject();
		
	}

}
