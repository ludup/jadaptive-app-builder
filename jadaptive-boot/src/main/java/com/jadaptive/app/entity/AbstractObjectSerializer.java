package com.jadaptive.app.entity;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.FieldDefinition;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.ApplicationServiceImpl;
import com.jadaptive.utils.Utils;

public class AbstractObjectSerializer extends StdSerializer<AbstractObject> {

	private static final long serialVersionUID = 5624312163275460262L;

	static Logger log = LoggerFactory.getLogger(AbstractObjectSerializer.class);
	
	public AbstractObjectSerializer() {
		super(AbstractObject.class);
	}

	@Override
	public void serialize(AbstractObject value, JsonGenerator gen, SerializerProvider provider) throws IOException {

		try {
			ObjectTemplate template = ApplicationServiceImpl.getInstance().getBean(
					TemplateService.class).get(value.getResourceKey());

			writeObject(value, template, gen);
		} catch (Throwable e) {
			log.error("Failed to serialize Entity", e);
			throw new IOException(e);
		}

	}
	
	private void writeObject(AbstractObject value, ObjectTemplate template, JsonGenerator gen) throws IOException {
		
		gen.writeStartObject();
		
		gen.writeStringField("uuid", value.getUuid());
		gen.writeStringField("resourceKey", value.getResourceKey());
		gen.writeBooleanField("system", value.isSystem());
		gen.writeBooleanField("hidden", value.isHidden());

		writeFields(gen, template.getFields(), value);

		gen.writeEndObject();
	}


	private void writeEmbeddedObject(AbstractObject value, ObjectTemplate template, JsonGenerator gen, boolean collection) throws IOException {

		if(collection) {
			gen.writeStartObject();
		} else {
			gen.writeObjectFieldStart(value.getResourceKey());
		}
		
		gen.writeStringField("uuid", value.getUuid());
		gen.writeStringField("resourceKey", value.getResourceKey());
		gen.writeBooleanField("system", value.isSystem());
		gen.writeBooleanField("hidden", value.isHidden());

		writeFields(gen, template.getFields(), value);

		gen.writeEndObject();
	}
	
	private void writeFields(JsonGenerator gen, Collection<FieldDefinition> templates, AbstractObject value) throws IOException {
		
		if(!Objects.isNull(templates)) {
			for (FieldDefinition t : templates) {

				switch(t.getFieldType()) {
				case OBJECT_EMBEDDED:
					String type = t.getValidationValue(ValidationType.OBJECT_TYPE);
					ObjectTemplate template = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(type);
					if(t.getCollection()) {
						gen.writeArrayFieldStart(t.getResourceKey());
						for(AbstractObject child : value.getObjectCollection(t.getResourceKey())) {
							writeEmbeddedObject(child, template, gen, true);
						}
						gen.writeEndArray();
					} else {
						writeEmbeddedObject(value.getChild(t), template, gen, false);
					}
					break;
				default:
					if(t.getCollection()) {
						gen.writeArrayFieldStart(t.getResourceKey());
//						if(t.getFieldType()==FieldType.OBJECT_REFERENCE) {
//							for(Object ref : value.getReferenceCollection(t.getResourceKey())) {
//								Map<?,?> m = (Map<?,?>) ref;
//								gen.writeStartObject();
//								gen.writeStringField("uuid", (String) m.get("uuid"));
//								gen.writeStringField("name", (String) m.get("name"));
//								gen.writeEndObject();
//							}
//						} else {
							for(Object v : value.getCollection(t.getResourceKey())) {
								writeCollectionField(gen, t, v);
							}
//						}
						gen.writeEndArray();
					} else {
						writeField(gen, t, value.getValue(t.getResourceKey()));
					}
				}

			}
		}
	}

	private void writeField(JsonGenerator gen, FieldDefinition t, Object value) throws IOException {
		
		switch (t.getFieldType()) {
		case BOOL:
			gen.writeBooleanField(t.getResourceKey(), (Boolean) value);
			break;
		case DECIMAL:
			gen.writeNumberField(t.getResourceKey(), (Double) value);
			break;
		case LONG:
			gen.writeNumberField(t.getResourceKey(), (Long) value);
			break;
		case INTEGER:
			gen.writeNumberField(t.getResourceKey(), (Integer) value);
			break;
		case TIMESTAMP:
			gen.writeStringField(t.getResourceKey(), Utils.formatDate((Date)value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
			break;
		case TEXT:
		case TEXT_AREA:
		case PASSWORD:
		case OBJECT_REFERENCE:
		case ENUM:
			gen.writeStringField(t.getResourceKey(), checkNull(value));
			break;
		default:
			throw new IllegalStateException(
					String.format("Unexpected field type %s in writeField", 
						t.getFieldType().name()));
		}
		
	}
	
	private String checkNull(Object obj) {
		if(Objects.nonNull(obj)) {
			return obj.toString();
		}
		return "";
	}
	private void writeCollectionField(JsonGenerator gen, FieldDefinition t, Object value) throws IOException {
		
		switch (t.getFieldType()) {
		case BOOL:
			gen.writeBoolean((Boolean) value);
			break;
		case DECIMAL:
			gen.writeNumber((Double)value);
			break;
		case LONG:
			gen.writeNumber((Long)value);
			break;
		case INTEGER:
			gen.writeNumber((Integer)value);
			break;
		case TIMESTAMP:
			gen.writeString(Utils.formatDate((Date)value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
			break;
		case TEXT:
		case TEXT_AREA:
		case PASSWORD:
		case OBJECT_REFERENCE:
		case ENUM:
			gen.writeString(value.toString());
			break;
		default:
			throw new IllegalStateException(
					String.format("Unexpected field type %s in writeField", 
						t.getFieldType().name()));
		}
		
	}
}
