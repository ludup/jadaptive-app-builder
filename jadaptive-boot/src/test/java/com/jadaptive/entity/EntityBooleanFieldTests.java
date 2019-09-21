package com.jadaptive.entity;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.FieldTemplate;
import com.jadaptive.entity.template.FieldType;
import com.jadaptive.entity.template.FieldValidator;
import com.jadaptive.entity.template.ValidationException;
import com.jadaptive.entity.template.ValidationType;

public class EntityBooleanFieldTests extends AbstractDeserializerTest {


	private ObjectMapper getBooleanField(boolean required, FieldValidator... validators) {
		
		EntityTemplate template = new EntityTemplate();
		
		template.setType(EntityType.COLLECTION);

		FieldTemplate t1 = new FieldTemplate();
		t1.setResourceKey("confirmed");
		t1.setDefaultValue("false");
		t1.setRequired(required);
		t1.setDescription("Has the business been confirmed");
		t1.setFieldType(FieldType.BOOL);
		t1.getValidators().addAll(Arrays.asList(validators));

		template.getFields().add(t1);

		Map<String,EntityTemplate> templates = new HashMap<>();
		templates.put("company", template);
		
		return getMapper(templates);
	}
	
	@Test
	public void deserializeRequiredBooleanField() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.booleanField("confirmed", true)
			.endObject().toString();

		System.out.println(json);
		Entity e = getBooleanField(true).readValue(json, Entity.class);

		Assert.assertEquals("true", e.getValue("confirmed"));

	}
	
	@Test
	public void deserializeNonRequiredBooleanField() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		Entity e = getBooleanField(false).readValue(json, Entity.class);

		Assert.assertEquals("false", e.getValue("confirmed"));

	}

	
	@Test(expected = IOException.class)
	public void deserializeMissingRequiredBooleanField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		getBooleanField(true).readValue(json, Entity.class);


	}
	
	@Test(expected = IOException.class)
	public void deserializeInvalidBooleanField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("confirmed", "xyz")
			.endObject().toString();

		System.out.println(json);
		
		getBooleanField(true).readValue(json, Entity.class);


	}
	
	public void deserializeMissingNotRequiredBooleanField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		Entity e = getBooleanField(false).readValue(json, Entity.class);

		Assert.assertEquals("false", e.getValue("confirmed"));
	}


}
