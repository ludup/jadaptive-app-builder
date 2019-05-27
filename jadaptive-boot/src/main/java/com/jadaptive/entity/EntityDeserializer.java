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
		} catch (RepositoryException | EntityNotFoundException | ParseException e) {
			throw new IOException(e);
		}
	}

	private void iterateCategories(JsonNode current, String uuid, Set<FieldCategory> categories,
			Map<String, Map<String, String>> properties) {
		for(FieldCategory c : categories) {
			iterateCategory(current.findValue(c.getResourceKey()), uuid, c, properties);
		}
	}

	private void iterateCategory(JsonNode current, String uuid, FieldCategory c,
			Map<String, Map<String, String>> properties) {
		iterateFields(current, uuid, c.getTemplates(), properties);
	}

	private void iterateFields(JsonNode current, String uuid, Set<FieldTemplate> fields, Map<String,Map<String,String>> properties) {
		
		for(FieldTemplate field : fields) {
			switch(field.getFieldType()) {
			/**
			 * TODO support for objects and arrays
			 */
			default:
				setProperty(current, uuid, field.getResourceKey(), field.getDefaultValue(), properties);
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
