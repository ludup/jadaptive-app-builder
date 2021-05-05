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
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.entity.MongoEntity;

public class EntityDecimalFieldTests extends AbstractDeserializerTest {


	private ObjectMapper getDecimalField(boolean required, FieldValidator... validators) {
		
		ObjectTemplate template = new ObjectTemplate();
		
		template.setType(ObjectType.COLLECTION);

		FieldTemplate t1 = new FieldTemplate();
		t1.setResourceKey("revenue");
		t1.setDefaultValue("1.25");
		t1.setRequired(required);
		t1.setFieldType(FieldType.DECIMAL);
		t1.getValidators().addAll(Arrays.asList(validators));

		template.getFields().add(t1);

		Map<String,ObjectTemplate> templates = new HashMap<>();
		templates.put("company", template);
		
		return getMapper(templates);
	}
	
	@Test
	public void deserializeRequiredDecimalField() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.numberField("revenue", 1.5D)
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getDecimalField(true).readValue(json, MongoEntity.class);

		Assert.assertEquals(1.5D, e.getValue("revenue"));

	}
	
	@Test
	public void deserializeNonRequiredDecimalField() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getDecimalField(false).readValue(json, MongoEntity.class);

		Assert.assertEquals("1.25", e.getValue("revenue"));

	}
	
	@Test
	public void deserializeDecimalFieldWithRangeValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.numberField("revenue", 1.92)
			.endObject().toString();

		System.out.println(json);
		MongoEntity e = getDecimalField(true, new FieldValidator(ValidationType.RANGE, "0-9999999")).readValue(json, MongoEntity.class);

		Assert.assertEquals(1.92D, e.getValue("revenue"));

	}
	
	
	@Test(expected = IOException.class)
	public void deserializeDecimalFieldWithFailedRangeValidation() throws IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
				.numberField("revenue", -1)
			.endObject().toString();

		System.out.println(json);
		getDecimalField(true, new FieldValidator(ValidationType.RANGE, "0-9999999")).readValue(json, MongoEntity.class);

	}
	

	
	@Test(expected = IOException.class)
	public void deserializeMissingRequiredDecimalField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		getDecimalField(true).readValue(json, MongoEntity.class);


	}
	
	public void deserializeMissingNotRequiredDecimalField() throws JsonParseException, JsonMappingException, ValidationException, IOException {

		String json = new JSONObjectBuilder().startObject()
				.textField("resourceKey", "company")
			.endObject().toString();

		System.out.println(json);
		
		MongoEntity e = getDecimalField(false).readValue(json, MongoEntity.class);

		Assert.assertEquals("1.25", e.getValue("revenue"));
	}


}
