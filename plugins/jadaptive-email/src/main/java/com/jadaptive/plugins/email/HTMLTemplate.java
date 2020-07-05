package com.jadaptive.plugins.email;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = HTMLTemplate.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class HTMLTemplate extends NamedUUIDEntity {

	private static final long serialVersionUID = 1070995818848710214L;

	public static final String RESOURCE_KEY = "htmlTemplates";
	
	@ObjectField(name = "HTML", description = "The HTML content of this template", required = true, type = FieldType.TEXT)
	String html;

	@ObjectField(name = "Content Selector", description = "The selector that identifies the element to insert message content", required = true, defaultValue = "body", type = FieldType.TEXT)
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
}
