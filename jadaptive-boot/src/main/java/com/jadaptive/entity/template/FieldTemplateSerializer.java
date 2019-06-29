package com.jadaptive.entity.template;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jadaptive.entity.ValidationType;

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
		
		for(ValidationType v : value.getFieldType().getOptions()) {
			switch(v) {
			case LENGTH:
				break;
			case RANGE:
				break;
			case REGEX:
				break;
			default:
				break;
			}
		}
		
		gen.writeEndObject();
		
	}

}
