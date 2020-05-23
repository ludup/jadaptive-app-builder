package com.jadaptive.app.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.ApplicationServiceImpl;
import com.jadaptive.utils.Utils;

public class AbstractObjectDeserializer extends StdDeserializer<AbstractObject> {

	private static final long serialVersionUID = -7322676764669077046L;

	static final Logger log = LoggerFactory.getLogger(AbstractObjectDeserializer.class);
	
	TemplateService templateService; 
	
	public AbstractObjectDeserializer(TemplateService templateService) {
		super((Class<?>)null);
		this.templateService = templateService;
	}
	
	public AbstractObjectDeserializer() {
		super((Class<?>)null);
		templateService = ApplicationServiceImpl.getInstance().getBean(TemplateService.class);
	}
	
	public AbstractObjectDeserializer(Class<?> vc) {
		super(vc);
		templateService = ApplicationServiceImpl.getInstance().getBean(TemplateService.class);
	}

	public AbstractObjectDeserializer(JavaType valueType) {
		super(valueType);
		templateService = ApplicationServiceImpl.getInstance().getBean(TemplateService.class);
		
	}

	public AbstractObjectDeserializer(StdDeserializer<?> src) {
		super(src);
		templateService = ApplicationServiceImpl.getInstance().getBean(TemplateService.class);
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
			
			ObjectTemplate template = templateService.get(rkNode.asText());
			
			MongoEntity e = new MongoEntity(template.getResourceKey(), new Document());
			
			iterateType(node, template, e, true);

			return e;
		} catch (Throwable e) {
			log.error("Failed to deserialize Entity", e);
			throw new IOException(e);
		}
	}

	private void iterateType(JsonNode node, ObjectTemplate template, MongoEntity e, boolean requiresUUID) throws IOException, ValidationException {
		

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

		node = node.findPath(field.getResourceKey());
		if(Objects.isNull(node) || node instanceof MissingNode) {
			if(field.isRequired()) {
				throw new ValidationException(String.format("Missing node for %s", field.getResourceKey()));
			} else {
				setPropertyDefault(field, e);
				return;
			}
		} 
		
		if(field.getCollection() && !node.isArray()) {
			throw new ValidationException(String.format("%s is a collection and the json node is not an array", field.getResourceKey()));
		} else if(!field.getCollection() && node.isArray()) {
			throw new ValidationException(String.format("%s is not a collection but the json node is an array", field.getResourceKey()));
		} 

		if(field.getFieldType()==FieldType.OBJECT_EMBEDDED) {
			processEmbeddedObjects(field, node, e);
		} else {
			processSimpleTypes(field, node, e);
		}

	}

	private void processEmbeddedObjects(FieldTemplate field, JsonNode node, MongoEntity e) throws ValidationException, IOException {
		
		String type = field.getValidationValue(ValidationType.OBJECT_TYPE);
		
		try {
 			ObjectTemplate template = templateService.get(type);
 			
 			if(node.isArray()) {
 				List<Document> documents = new ArrayList<>();
 				for(JsonNode element : node) {
 					MongoEntity child = new MongoEntity(e, field.getResourceKey(), new Document());
 					iterateType(element, template, child, false);
 					documents.add(new Document(child.getDocument()));
 				}
 				e.setValue(field, documents);
 			} else {
 				iterateType(node, template, new MongoEntity(e, field.getResourceKey(), new Document()), false);
 			}
 			
 		} catch(ObjectException ex) {
 			throw new ValidationException(String.format("%s object type template not found", type));
 		}	
	}

//	private void processReferenceObjects(FieldTemplate field, JsonNode node, MongoEntity e) {
//		// EntityService currently validates references
//		setProperty(node.asText(), field, e);
//	}

	private void processSimpleTypes(FieldTemplate field, JsonNode node, MongoEntity e) throws IOException, ValidationException {
		if(node.isArray()) {
			setCollectionProperty(field, node, e);
		} else {
			setProperty(validate(field, node, e), field, e);
		}
	}

	private void setCollectionProperty(FieldTemplate field, JsonNode node, MongoEntity e) throws IOException, ValidationException {
		
		List<Object> values = new ArrayList<>();
		for(JsonNode element : node) {
			values.add(validate(field, element, e));
		}
		e.setValue(field, values);
		
	}

	private Object validate(FieldTemplate field, JsonNode node, MongoEntity e) throws IOException, ValidationException {
		
		switch(field.getFieldType()) {
		case BOOL:
			validateBoolean(node, field);
			return node.asBoolean();
		case DECIMAL:
			validateDecimal(node, field);
			return node.asDouble();
		case LONG:
			validateNumber(node, field);
			return node.asLong();
		case INTEGER:
			validateNumber(node, field);
			return node.asInt();
		case TIMESTAMP:
			validateDate(node, field);
			return Utils.parseDate(node.asText(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		case ENUM:
			validateEnum(node, field);
			return node.asText();
		case TEXT:
		case TEXT_AREA:
		case PASSWORD:
			validateText(node, field);
			return node.asText();
		case OBJECT_REFERENCE:
//			if(node.isObject()) {
//				return validateReference(node, field);
//			}
			return node.asText();
		default:
			throw new ValidationException(
					String.format("Missing field type %s in validate method", 
						field.getFieldType().name()));
		}
		
	}

	
//	private ObjectReference2 validateReference(JsonNode node, FieldTemplate field) {
//		return new ObjectReference2(node.get("uuid").asText(), node.get("name").asText());
//		
//	}

	private void validateDate(JsonNode node, FieldTemplate field) {
		
	}

	private void validateEnum(JsonNode node, FieldTemplate field) {
	
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

	private void setProperty(Object value, FieldTemplate t, MongoEntity e) {
		if(!Objects.isNull(value)) {
			e.setValue(t, value);
		} 
	}
	
	private void setPropertyDefault(FieldTemplate t, MongoEntity e) {
		e.setValue(t, t.getDefaultValue()); 
	}


}
