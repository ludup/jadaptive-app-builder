package com.jadaptive.entity;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import com.jadaptive.repository.RepositoryException;

public class EntityDeserializer extends StdDeserializer<Entity> {

	private static final long serialVersionUID = -7322676764669077046L;

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
			
			if(Objects.isNull(uuidNode)) {
				throw new IOException("Missing uuid in JSON deserialise");
			}
			
			String uuid = uuidNode.asText();
			
			JsonNode rkNode = node.findValue("resourceKey");
			
			if(Objects.isNull(rkNode)) {
				throw new IOException("Missing resourceKey in JSON deserialise");
			}
			
			EntityTemplateService templateService = ApplicationServiceImpl.getInstance().getBean(EntityTemplateService.class);
			
			EntityTemplate template = templateService.get(rkNode.asText());
			
			JsonNode current = node;
			
			Map<String,Map<String,String>> properties = new HashMap<>();
			properties.put(uuid, new HashMap<>());
			
			setProperty(current, uuid, "uuid", uuid, properties);
			setProperty(current, uuid, "resourceKey", rkNode.asText(), properties);
			setProperty(current, uuid, "system", "false", properties);
			setProperty(current, uuid, "hidden", "false", properties);
			
			iterateFields(current, uuid, template.getFields(), properties);

			iterateCategories(current, uuid, template.getCategories(), properties);

			Entity e = new Entity();
			e.load(uuid, properties);
			return e;
		} catch (RepositoryException | EntityNotFoundException | ParseException | ValidationException e) {
			throw new IOException(e);
		}
	}

	private void iterateCategories(JsonNode current, String uuid, Set<FieldCategory> categories,
			Map<String, Map<String, String>> properties) throws IOException, ValidationException {
		for(FieldCategory c : categories) {
			iterateCategory(current.findValue(c.getResourceKey()), uuid, c, properties);
		}
	}

	private void iterateCategory(JsonNode current, String uuid, FieldCategory c,
			Map<String, Map<String, String>> properties) throws IOException, ValidationException {
		iterateFields(current, uuid, c.getTemplates(), properties);
	}

	private void iterateFields(JsonNode current, String uuid, Set<FieldTemplate> fields, Map<String,Map<String,String>> properties) throws IOException, ValidationException {
		
		for(FieldTemplate field : fields) {
			validateNode(current.findPath(field.getResourceKey()), uuid, field, properties);
		}
	}

	private void validateNode(JsonNode node, String uuid, FieldTemplate field,
			Map<String, Map<String, String>> properties) throws IOException, ValidationException {
		
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
		
		setProperty(node, uuid, field.getResourceKey(), node.asText(field.getDefaultValue()), properties);
		
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

	private void setProperty(JsonNode current, String uuid, String resourceKey, String defaultValue, Map<String,Map<String,String>> properties) {
		JsonNode value = current.findValue(resourceKey);
		if(value==null) {
			properties.get(uuid).put(resourceKey, defaultValue);
		} else {
			properties.get(uuid).put(resourceKey, value.asText(defaultValue));
		}
	}


}
