package com.jadaptive.entity;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.jadaptive.app.ApplicationServiceImpl;
import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.EntityTemplateService;
import com.jadaptive.entity.template.FieldCategory;
import com.jadaptive.entity.template.FieldTemplate;
import com.jadaptive.entity.template.FieldValidator;
import com.jadaptive.entity.template.ValidationException;

public class EntityDeserializer extends StdDeserializer<Entity> {

	private static final long serialVersionUID = -7322676764669077046L;

	static final Logger log = LoggerFactory.getLogger(EntityDeserializer.class);
	
	public EntityDeserializer() {
		super((Class<?>)null);
	}
	
	public EntityDeserializer(Class<?> vc) {
		super(vc);
	}

	public EntityDeserializer(JavaType valueType) {
		super(valueType);
	}

	public EntityDeserializer(StdDeserializer<?> src) {
		super(src);
	}

	@Override
	public Entity deserialize(JsonParser parser, DeserializationContext ctx)
			throws IOException, JsonProcessingException {

		try {
			
			
			ObjectCodec oc = parser.getCodec();
			JsonNode node = oc.readTree(parser);
   
			JsonNode uuidNode = node.findValue("uuid");
			JsonNode rkNode = node.findValue("resourceKey");
			
			if(Objects.isNull(rkNode)) {
				throw new IOException("Missing resourceKey in JSON deserialise");
			}
			
			EntityTemplateService templateService = ApplicationServiceImpl.getInstance().getBean(EntityTemplateService.class);
			
			EntityTemplate template = templateService.get(rkNode.asText());
			
			Entity e = new Entity(template.getUuid(), new Document());
			
			if(!Objects.isNull(uuidNode)) {
				e.setUuid(uuidNode.asText());
			}
			
			e.setSystem(node.findValue("system").asBoolean(false));
			e.setHidden(node.findValue("hidden").asBoolean(false));
			
			iterateFields(node, template.getFields(), e);

			iterateCategories(node, template.getCategories(), e);

			return e;
		} catch (Throwable e) {
			log.error("Failed to deserialize Entity", e);
			throw new IOException(e);
		}
	}

	private void iterateCategories(JsonNode current, Set<FieldCategory> categories,
			Entity e) throws IOException, ValidationException {
		if(!Objects.isNull(categories)) {
			for(FieldCategory c : categories) {
				iterateCategory(current.findValue(c.getResourceKey()), c, new Entity(e, c.getResourceKey(), new Document()));
			}
		}
	}

	private void iterateCategory(JsonNode current, FieldCategory c,
			Entity cat) throws IOException, ValidationException {
		iterateFields(current, c.getFields(), cat);
	}

	private void iterateFields(JsonNode current, Set<FieldTemplate> fields, Entity e) throws IOException, ValidationException {
		
		if(!Objects.isNull(fields)) {
			for(FieldTemplate field : fields) {
				validateNode(current.findPath(field.getResourceKey()), field, e);
			}
		}
	}

	private void validateNode(JsonNode node, FieldTemplate field,
			Entity e) throws IOException, ValidationException {
		
		if(node==null) {
			throw new IOException(String.format("%s is missing", field.getResourceKey()));
		}
		
		switch(field.getFieldType()) {
		case BOOLEAN:
			validateBooleean(node, field);
			break;
		case DECIMAL:
			validateDecimal(node, field);
			break;
		case NUMBER:
			validateNumber(node, field);
			break;
		case TEXT:
		case TEXT_AREA:
		default:
			validateText(node, field);
		}
		
		setProperty(node, field, e);
		
	}

	private void validateNumber(JsonNode node, FieldTemplate field) throws ValidationException {
		try {
			Long.parseLong(node.asText());
		} catch (NumberFormatException e) {
			throw new ValidationException(String.format("Value %s for field %s is not a number", node.asText(), field.getResourceKey()));
		}
	}

	private void validateDecimal(JsonNode node, FieldTemplate field) throws ValidationException {
		try {
			Double.parseDouble(node.asText());
		} catch (NumberFormatException e) {
			throw new ValidationException(String.format("Value %s for field %s is not a double", node.asText(), field.getResourceKey()));
		}
	}

	private void validateBooleean(JsonNode node, FieldTemplate field) throws ValidationException {
		if(node.isBoolean()) {
			return;
		}
		
		switch(node.asText()) {
		case "true":
		case "TRUE":
		case "false":
		case "FALSE":
			return;
		default:
			throw new ValidationException(String.format("Value %s for field %s is not a boolean", node.asText(), field.getResourceKey()));
		}
	}

	private void validateText(JsonNode node, FieldTemplate field) throws ValidationException {
		if(!Objects.isNull(field.getValidators())) {
			for(FieldValidator v : field.getValidators()) {
				switch(v.getType()) {
				case LENGTH:
					String value = node.asText();
					int maxlength = Integer.parseInt(v.getValue());
					if(value.length() > maxlength) {
						throw new ValidationException(String.format("%s must be less than %d characters", field.getResourceKey(), maxlength));
					}
					break;
				case RANGE:
					break;
				case REGEX:
					break;
				}
			}
		}
	}

	private void setProperty(JsonNode value, FieldTemplate t, Entity e) {
		if(!Objects.isNull(value)) {
			e.setValue(t, value.asText());
		} 
	}


}
