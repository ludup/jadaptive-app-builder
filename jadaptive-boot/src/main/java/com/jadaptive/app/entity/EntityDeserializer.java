package com.jadaptive.app.entity;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.ApplicationServiceImpl;

public class EntityDeserializer extends StdDeserializer<MongoEntity> {

	private static final long serialVersionUID = -7322676764669077046L;

	static final Logger log = LoggerFactory.getLogger(EntityDeserializer.class);
	
	EntityTemplateService templateService; 
	
	public EntityDeserializer(EntityTemplateService templateService) {
		super((Class<?>)null);
		this.templateService = templateService;
	}
	
	public EntityDeserializer() {
		super((Class<?>)null);
		templateService = ApplicationServiceImpl.getInstance().getBean(EntityTemplateService.class);
	}
	
	public EntityDeserializer(Class<?> vc) {
		super(vc);
		templateService = ApplicationServiceImpl.getInstance().getBean(EntityTemplateService.class);
	}

	public EntityDeserializer(JavaType valueType) {
		super(valueType);
		templateService = ApplicationServiceImpl.getInstance().getBean(EntityTemplateService.class);
		
	}

	public EntityDeserializer(StdDeserializer<?> src) {
		super(src);
		templateService = ApplicationServiceImpl.getInstance().getBean(EntityTemplateService.class);
	}

	@Override
	public MongoEntity deserialize(JsonParser parser, DeserializationContext ctx)
			throws IOException, JsonProcessingException {

		try {
			
			ObjectCodec oc = parser.getCodec();
			JsonNode node = oc.readTree(parser);
   
			JsonNode rkNode = node.findValue("resourceKey");
			
			if(Objects.isNull(rkNode)) {
				throw new IOException("Missing resourceKey in JSON deserialise");
			}
			
			EntityTemplate template = templateService.get(rkNode.asText());
			
			MongoEntity e = new MongoEntity(template.getResourceKey(), new Document());
			
			iterateType(node, template, e, true);

			return e;
		} catch (Throwable e) {
			log.error("Failed to deserialize Entity", e);
			throw new IOException(e);
		}
	}

	private void iterateType(JsonNode node, EntityTemplate template, MongoEntity e, boolean requiresUUID) throws IOException, ValidationException {
		

		JsonNode uuidNode = node.findValue("uuid");
		if(!(uuidNode instanceof NullNode) && !Objects.isNull(uuidNode) && StringUtils.isNotBlank(uuidNode.asText())) {
			e.setUuid(uuidNode.asText());
		}
		
		e.setSystem(getBooleanNodeValue(node, "system"));
		e.setHidden(getBooleanNodeValue(node, "hidden"));
		
		iterateFields(node, template.getFields(), e);
		
	}

	private Boolean getBooleanNodeValue(JsonNode node, String val) {
		
		node = node.findValue(val);
		if(Objects.isNull(node)) {
			return false;
		}
		return node.asBoolean(false);
	}


	private void iterateFields(JsonNode current, Collection<FieldTemplate> fields, MongoEntity e) throws IOException, ValidationException {
		
		if(!Objects.isNull(fields)) {
			for(FieldTemplate field : fields) {
				validateNode(current, field, e);
			}
		}
	}

	private void validateNode(JsonNode node, FieldTemplate field,
			MongoEntity e) throws IOException, ValidationException {
		
		if(log.isInfoEnabled()) {
			log.info("Validating node {}", field.getResourceKey());
		}
		
		if(node==null) {
			if(field.getRequired()) {
				throw new IOException(String.format("%s is missing", field.getResourceKey()));
			} 
			setPropertyDefault(field, e);
			return;
		}
		
		node = node.findPath(field.getResourceKey());
		if(Objects.isNull(node) || node instanceof MissingNode) {
			if(field.getRequired()) {
				throw new ValidationException(String.format("Missing node for %s", field.getResourceKey()));
			} else {
				setPropertyDefault(field, e);
				return;
			}
		} 
		
//		if(!Objects.isNull(field.getValidators()) && !field.getValidators().isEmpty()) {

			switch(field.getFieldType()) {
			case OBJECT_REFERENCE:
				validateObjectReference(node, field);
				break;
			case OBJECT_EMBEDDED:
				validateObject(node, field, e);
				/**
				 * Return otherwise setProperty at the end kills the document
				 */
				return;
			case BOOL:
				validateBoolean(node, field);
				break;
			case DECIMAL:
				validateDecimal(node, field);
				break;
			case NUMBER:
				validateNumber(node, field);
				break;
			case COUNTRY:
				validateCountry(node, field);
				break;
			case ENUM:
				validateEnum(node, field);
				break;
			case TEXT:
			case TEXT_AREA:
			default:
				validateText(node, field);
			}
//		}
		
		setProperty(node, field, e);
		
	}

	private void validateEnum(JsonNode node, FieldTemplate field) {
		
		
	}

	private void validateObjectReference(JsonNode node, FieldTemplate field) {
		
		/**
		 * Object reference is enforced by the Entity service.
		 */
		
	}
	
	private void validateObject(JsonNode node, FieldTemplate field, MongoEntity e) throws IOException, ValidationException {
		
 		String type = field.getValidationValue(ValidationType.OBJECT_TYPE);
		EntityTemplate template = templateService.get(type);

		iterateType(node, template, new MongoEntity(e, field.getResourceKey(), new Document()), false);
	}

	private void validateCountry(JsonNode node, FieldTemplate field) {
		
		
	}

	private void validateNumber(JsonNode node, FieldTemplate field) throws ValidationException {
		try {
			long value = Long.parseLong(node.asText());
			
			if(!Objects.isNull(field.getValidators())) {
				for(FieldValidator v : field.getValidators()) {
					switch(v.getType()) {
					case RANGE:
						String[] range = v.getValue().split(",");
						if(range.length != 2) {
							throw new ValidationException(String.format("Invalid range %s value in validator use \"<min>,<max>\" format", v.getValue()));
						}
						try {
							long min = Long.parseLong(range[0]);
							long max = Long.parseLong(range[1]);
							if(value < min || value > max) {
								throw new ValidationException(String.format("%s must be in the range %d to %d", field.getResourceKey(), min, max));
							}
						} catch (NumberFormatException e) {
							throw new ValidationException(String.format("Invalid range %s value in validator use \"<min>,<max>\" format", v.getValue()));
						}
						break;
					default:
						break;
					}
				}
			}
		} catch (NumberFormatException e) {
			throw new ValidationException(String.format("Value %s for field %s is not a number", node.asText(), field.getResourceKey()));
		}
	}

	private void validateDecimal(JsonNode node, FieldTemplate field) throws ValidationException {
		try {
			double value = Double.parseDouble(node.asText());
			
			if(!Objects.isNull(field.getValidators())) {
				for(FieldValidator v : field.getValidators()) {
					switch(v.getType()) {
					case RANGE:
						String[] range = v.getValue().split(",");
						if(range.length != 2) {
							throw new ValidationException(String.format("Invalid range %s value in validator use \"<min>,<max>\" format", v.getValue()));
						}
						try {
							double min = Double.parseDouble(range[0]);
							double max = Double.parseDouble(range[1]);
							if(value < min || value > max) {
								throw new ValidationException(String.format("%s must be in the range %d to %d", field.getResourceKey(), min, max));
							}
						} catch (NumberFormatException e) {
							throw new ValidationException(String.format("Invalid range %s value in validator use \"<min>,<max>\" format", v.getValue()));
						}
						break;
					default:
						break;
					}
				}
			}
		} catch (NumberFormatException e) {
			throw new ValidationException(String.format("Value %s for field %s is not a double", node.asText(), field.getResourceKey()));
		}
	}

	private void validateBoolean(JsonNode node, FieldTemplate field) throws ValidationException {
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
		
		String value = node.asText();
		
		if(!Objects.isNull(field.getValidators())) {
			for(FieldValidator v : field.getValidators()) {
				switch(v.getType()) {
				case LENGTH:
					
					int maxlength = Integer.parseInt(v.getValue());
					if(value.length() > maxlength) {
						throw new ValidationException(String.format("%s must be less than %d characters", field.getResourceKey(), maxlength));
					}
					break;
				case REGEX:
					Pattern pattern = Pattern.compile(v.getValue());
					if(!pattern.matcher(value).matches()) {
						throw new ValidationException(String.format("%s does not conform to regex pattern %s", value, v.getValue()));
					}
					break;
				default:
					break;
				}
			}
		}
	}

	private void setProperty(JsonNode value, FieldTemplate t, MongoEntity e) {
		if(!Objects.isNull(value)) {
			e.setValue(t, value.asText());
		} 
	}
	
	private void setPropertyDefault(FieldTemplate t, MongoEntity e) {
		e.setValue(t, t.getDefaultValue()); 
	}


}
