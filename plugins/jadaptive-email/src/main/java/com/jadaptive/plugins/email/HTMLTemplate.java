package com.jadaptive.plugins.email;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "HTML Templates", resourceKey = Message.RESOURCE_KEY, type = EntityType.COLLECTION)
public class HTMLTemplate extends NamedUUIDEntity {

	public static final String RESOURCE_KEY = "htmlTemplates";
	
	@Column(name = "HTML", description = "The HTML content of this template", required = true, type = FieldType.TEXT)
	String html;

	@Column(name = "Content Selector", description = "The selector that identifies the element to insert message content", required = true, defaultValue = "body", type = FieldType.TEXT)
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
