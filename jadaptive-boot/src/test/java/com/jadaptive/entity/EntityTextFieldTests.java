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

public class EntityTextFieldTests extends AbstractDeserializerTest {

	
	private ObjectMapper getTextField(boolean required, FieldValidator... validators) {
		
		EntityTemplate template = new EntityTemplate();
		
		template.setType(EntityType.COLLECTION);

		FieldTemplate t1 = new FieldTemplate();
		t1.setResourceKey("name");
		t1.setDefaultValue("Default");
		t1.setRequired(required);
		t1.setDescription("The company name");
		t1.setFieldType(FieldType.TEXT);
		t1.getValidators().addAll(Arrays.asList(validators));

		template.getFields().add(t1);


		
		Map<String,EntityTemplate> templates = new HashMap<>();
		templates.put("company", template);
		
		return getMapper(templates);
	}
	
	@Test
	public void deserializeRequiredTextField() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getTextField(true).readValue(json, MongoEntity.class);

		Assert.assertEquals("JADAPTIVE", e.getValue("name"));

	}
	
	@Test
	public void deserializeTextFieldWithLengthValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getTextField(true, new FieldValidator(ValidationType.LENGTH, "255")).readValue(json, MongoEntity.class);

		Assert.assertEquals("JADAPTIVE", e.getValue("name"));

	}
	
	@Test
	public void deserializeTextFieldWithRegexValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		getTextField(true, new FieldValidator(ValidationType.REGEX, "\\w+")).readValue(json, MongoEntity.class);

	}
	
	@Test(expected = IOException.class)
	public void deserializeTextFieldWithFailedLengthValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		getTextField(true, new FieldValidator(ValidationType.LENGTH, "5")).readValue(json, MongoEntity.class);

	}
	
	@Test(expected = IOException.class)
	public void deserializeTextFieldWithFailedRegexValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		getTextField(true, new FieldValidator(ValidationType.REGEX, "\\d+")).readValue(json, MongoEntity.class);

	}
	
	@Test(expected = IOException.class)
	public void deserializeMissingRequiredTextField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		getTextField(true).readValue(json, MongoEntity.class);


	}
	
	public void deserializeNotRequiredTextField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		MongoEntity e = getTextField(false).readValue(json, MongoEntity.class);

		Assert.assertEquals("Default", e.getValue("name"));
	}

}
