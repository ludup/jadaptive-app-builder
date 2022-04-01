package com.jadaptive.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.entity.MongoEntity;

public class EntityNumberFieldTests extends AbstractDeserializerTest {


	private ObjectMapper getNumberField(boolean required, FieldValidator... validators) {
		
		ObjectTemplate template = new ObjectTemplate();
		
		template.setType(ObjectType.COLLECTION);

		FieldTemplate t1 = new FieldTemplate();
		t1.setResourceKey("employees");
		t1.setDefaultValue("1");
		t1.setRequired(required);
		t1.setFieldType(FieldType.LONG);
		t1.getValidators().addAll(Arrays.asList(validators));

		template.getFields().add(t1);

		Map<String,ObjectTemplate> templates = new HashMap<>();
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

		assertEquals(100L, e.getValue("employees"));

	}
	
	@Test
	public void deserializeNonRequiredNumberField() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getNumberField(false).readValue(json, MongoEntity.class);

		assertEquals("1", e.getValue("employees"));

	}
	
	@Test
	public void deserializeNumberFieldWithRangeValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.numberField("employees", 100)
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getNumberField(true, new FieldValidator(ValidationType.RANGE, "0-9999999")).readValue(json, MongoEntity.class);

		assertEquals(100L, e.getValue("employees"));

	}
	
	
	@Test
	public void deserializeNumberFieldWithFailedRangeValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.numberField("employees", -1)
			.endObject().toString();

		System.out.println(json);
		getNumberField(true, new FieldValidator(ValidationType.RANGE, "0-9999999")).readValue(json, MongoEntity.class);

	}
	

	
	@Test
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

		assertEquals("1", e.getValue("employees"));
	}


}
