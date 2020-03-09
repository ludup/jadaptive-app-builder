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
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.entity.MongoEntity;

public class EntityNumberFieldTests extends AbstractDeserializerTest {


	private ObjectMapper getNumberField(boolean required, FieldValidator... validators) {
		
		EntityTemplate template = new EntityTemplate();
		
		template.setType(EntityType.COLLECTION);

		FieldTemplate t1 = new FieldTemplate();
		t1.setResourceKey("employees");
		t1.setDefaultValue("1");
		t1.setRequired(required);
		t1.setDescription("The number of employees");
		t1.setFieldType(FieldType.NUMBER);
		t1.getValidators().addAll(Arrays.asList(validators));

		template.getFields().add(t1);

		Map<String,EntityTemplate> templates = new HashMap<>();
		templates.put("company", template);
		
		return getMapper(templates);
	}
	
	@Test
	public void deserializeRequiredNumberField() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.numberField("employees", 100)
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getNumberField(true).readValue(json, MongoEntity.class);

		Assert.assertEquals(100L, e.getValue("employees"));

	}
	
	@Test
	public void deserializeNonRequiredNumberField() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getNumberField(false).readValue(json, MongoEntity.class);

		Assert.assertEquals("1", e.getValue("employees"));

	}
	
	@Test
	public void deserializeNumberFieldWithRangeValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.numberField("employees", 100)
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getNumberField(true, new FieldValidator(ValidationType.RANGE, "0,9999999")).readValue(json, MongoEntity.class);

		Assert.assertEquals(100L, e.getValue("employees"));

	}
	
	
	@Test(expected = IOException.class)
	public void deserializeNumberFieldWithFailedRangeValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.numberField("employees", -1)
			.endObject().toString();

		System.out.println(json);
		getNumberField(true, new FieldValidator(ValidationType.RANGE, "0,9999999")).readValue(json, MongoEntity.class);

	}
	

	
	@Test(expected = IOException.class)
	public void deserializeMissingRequiredNumberField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		getNumberField(true).readValue(json, MongoEntity.class);


	}
	
	public void deserializeMissingNotRequiredNumberField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		MongoEntity e = getNumberField(false).readValue(json, MongoEntity.class);

		Assert.assertEquals("1", e.getValue("employees"));
	}


}
