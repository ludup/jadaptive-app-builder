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
import com.jadaptive.app.db.MongoEntity;

public class EntityTextFieldTests extends AbstractDeserializerTest {

	
	private ObjectMapper buildCompanyTemplate(boolean required, FieldValidator... validators) {
		
		ObjectTemplate template = new ObjectTemplate();
		
		template.setType(ObjectType.COLLECTION);

		FieldTemplate t1 = new FieldTemplate();
		t1.setResourceKey("name");
		t1.setDefaultValue("Default");
		t1.setFieldType(FieldType.TEXT);
		t1.getValidators().addAll(Arrays.asList(validators));

		template.getFields().add(t1);

		Map<String,ObjectTemplate> templates = new HashMap<>();
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
		MongoEntity e = buildCompanyTemplate(true).readValue(json, MongoEntity.class);

		assertEquals("JADAPTIVE", e.getValue("name"));

	}
	
	@Test
	public void deserializeTextFieldWithLengthValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = buildCompanyTemplate(true, new FieldValidator(ValidationType.LENGTH, "255", "default")).readValue(json, MongoEntity.class);

		assertEquals("JADAPTIVE", e.getValue("name"));

	}
	
	@Test
	public void deserializeTextFieldWithRegexValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		buildCompanyTemplate(true, new FieldValidator(ValidationType.REGEX, "\\w+", "default")).readValue(json, MongoEntity.class);

	}
	
	@Test
	public void deserializeTextFieldWithFailedLengthValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		buildCompanyTemplate(true, new FieldValidator(ValidationType.LENGTH, "5", "default")).readValue(json, MongoEntity.class);

	}
	
	@Test
	public void deserializeTextFieldWithFailedRegexValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.textField("name", "JADAPTIVE")
			.endObject().toString();

		System.out.println(json);
		buildCompanyTemplate(true, new FieldValidator(ValidationType.REGEX, "\\d+", "default")).readValue(json, MongoEntity.class);

	}
	
	@Test
	public void deserializeMissingRequiredTextField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		buildCompanyTemplate(true).readValue(json, MongoEntity.class);


	}
	
	public void deserializeNotRequiredTextField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		MongoEntity e = buildCompanyTemplate(false).readValue(json, MongoEntity.class);

		assertEquals("Default", e.getValue("name"));
	}

}
