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
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.utils.Utils;

public class AbstractObjectSerializer extends StdSerializer<AbstractObject> {

	private static final long serialVersionUID = 5624312163275460262L;

	static Logger log = LoggerFactory.getLogger(AbstractObjectSerializer.class);
	
	TemplateService templateService;
	
	public AbstractObjectSerializer() {
		super(AbstractObject.class);
	}
	
	private TemplateService getTemplateService() {
		if(Objects.isNull(templateService)) {
			templateService = ApplicationServiceImpl.getInstance().getBean(TemplateService.class);
		}
		return templateService;
	}

	@Override
	public void serialize(AbstractObject value, JsonGenerator gen, SerializerProvider provider) throws IOException {

		try {
			ObjectTemplate template = getTemplateService().get(value.getResourceKey());

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
		//gen.writeBooleanField("hidden", value.isHidden());

		writeFields(gen, template.getFields(), value);

		gen.writeEndObject();
	}


	private void writeEmbeddedObject(AbstractObject value, ObjectTemplate template, JsonGenerator gen, boolean collection) throws IOException {

		if(Objects.isNull(value)) {
			gen.writeNullField(template.getResourceKey());
			return;
		}
		if(collection) {
			gen.writeStartObject();
		} else {
			gen.writeObjectFieldStart(value.getResourceKey());
		}
		
		gen.writeStringField("uuid", value.getUuid());
		gen.writeStringField("resourceKey", value.getResourceKey());
		gen.writeBooleanField("system", value.isSystem());
		//gen.writeBooleanField("hidden", value.isHidden());

		writeFields(gen, template.getFields(), value);

		gen.writeEndObject();
	}
	
	private void writeFields(JsonGenerator gen, Collection<FieldTemplate> templates, AbstractObject value) throws IOException {
		
		if(!Objects.isNull(templates)) {
			for (FieldTemplate t : templates) {

				switch(t.getFieldType()) {
				case OBJECT_EMBEDDED:
//					AbstractObject embedded = value.getChild(t);
//					if(Objects.nonNull(embedded)) {
//						String clz = (String) embedded.getValue("_clz");
						if(t.getCollection()) {
							gen.writeArrayFieldStart(t.getResourceKey());
							for(AbstractObject child : value.getObjectCollection(t.getResourceKey())) {
								ObjectTemplate template = getTemplateService().get(child.getResourceKey());
								writeEmbeddedObject(child, template, gen, true);
							}
							gen.writeEndArray();
						} else {
							AbstractObject child = value.getChild(t);
							if(Objects.nonNull(child)) {
								ObjectTemplate template = getTemplateService().get(child.getResourceKey());
								writeEmbeddedObject(child, template, gen, false);
							}
						}
//					} else {
//						gen.writeNullField(t.getResourceKey());
//					}
					break;
				case OBJECT_REFERENCE:
					if(t.getCollection()) {
						gen.writeArrayFieldStart(t.getResourceKey());
							for(AbstractObject v : value.getObjectCollection(t.getResourceKey())) {
								writeCollectionField(gen, t, v);
							}
						gen.writeEndArray();
					} else {
						AbstractObject child = value.getChild(t);
						writeField(gen, t, child);
					}
					break;
				default:
					if(t.getCollection()) {
						gen.writeArrayFieldStart(t.getResourceKey());
							for(Object v : value.getCollection(t.getResourceKey())) {
								writeCollectionField(gen, t, v);
							}
						gen.writeEndArray();
					} else {
						writeField(gen, t, value.getValue(t.getResourceKey()));
					}
				}

			}
		}
	}

	private void writeField(JsonGenerator gen, FieldTemplate t, Object value) throws IOException {
		
		if(value == null) {
			gen.writeNullField(t.getResourceKey());
		} else {
			switch (t.getFieldType()) {
			case BOOL:
				gen.writeBooleanField(t.getResourceKey(), Boolean.valueOf(value.toString()));
				break;
			case DECIMAL:
				gen.writeNumberField(t.getResourceKey(), Double.valueOf(value.toString()));
				break;
			case LONG:
				gen.writeNumberField(t.getResourceKey(), Long.valueOf(value.toString()));
				break;
			case INTEGER:
				gen.writeNumberField(t.getResourceKey(), Integer.valueOf(value.toString()));
				break;
			case TIMESTAMP:
				gen.writeStringField(t.getResourceKey(), Utils.formatDate((Date)value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
				break;
			case DATE:
				gen.writeStringField(t.getResourceKey(), Utils.formatDate((Date)value, "yyyy-MM-dd"));
				break;
			case TEXT:
			case TEXT_AREA:
			case PASSWORD:
			case PERMISSION:
			case ENUM:
			case OPTIONS:
			case COUNTRY:
			case IMAGE:
			case FILE:
			case TIME:
				gen.writeStringField(t.getResourceKey(), value.toString());
				break;
			case OBJECT_REFERENCE:
				gen.writeObjectField(t.getResourceKey(), value);
				break;
			default:
				throw new IllegalStateException(
						String.format("Unexpected field type %s in writeField", 
							t.getFieldType().name()));
			}
		}
		
	}

	private void writeCollectionField(JsonGenerator gen, FieldTemplate t, Object value) throws IOException {
		
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
		case PERMISSION:
		case ENUM:
		case OPTIONS:
		case COUNTRY:
			gen.writeString(value.toString());
			break;
		case OBJECT_REFERENCE:
			if(value instanceof AbstractObject) {
				gen.writeStartObject();
				gen.writeStringField("uuid", ((AbstractObject)value).getUuid());
				gen.writeStringField("name", (String)((AbstractObject)value).getValue("name"));
				gen.writeEndObject();
			} else {
				gen.writeString(value.toString());
			}
			break;
		default:
			throw new IllegalStateException(
					String.format("Unexpected field type %s in writeField", 
						t.getFieldType().name()));
		}
		
	}
}
