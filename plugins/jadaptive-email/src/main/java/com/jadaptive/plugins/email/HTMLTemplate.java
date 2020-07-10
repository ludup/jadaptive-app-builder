package com.jadaptive.plugins.email;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = HTMLTemplate.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class HTMLTemplate extends NamedUUIDEntity {

	private static final long serialVersionUID = 1070995818848710214L;

	public static final String RESOURCE_KEY = "htmlTemplates";
	
	@ObjectField(required = true, readOnly = true, unique = true, type = FieldType.TEXT)
	@Validator(type = ValidationType.REGEX, value = "^[a-zA-Z0-9]+$")
	String shortName;
	
	@ObjectField(required = true, type = FieldType.TEXT_AREA)
	String html;

	@ObjectField(required = true, defaultValue = "body", type = FieldType.TEXT)
	String contentSelector;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getContentSelector() {
		return contentSelector;
	}

	public void setContentSelector(String contentSelector) {
		this.contentSelector = contentSelector;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
}
